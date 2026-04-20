package com.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.model.enums.Topic;

/**
 * Builds study plans from real {@link Question} data. Personalized daily plans exclude problems
 * the user already finished and shuffle deterministically per calendar day so the lineup changes daily.
 */
public class StudyPlanner {

    private final QuestionList questionList;

    public StudyPlanner(QuestionList questionList) {
        this.questionList = questionList;
    }

    /**
     * Legacy entry point: today's plan without excluding anyone's completions (neutral seed).
     */
    public LearningPlan generatePlan(String language, int level) {
        UUID neutral = new UUID(0L, 0L);
        return generateDailyPersonalizedPlan(LocalDate.now(), neutral, language, level, null,
                Collections.emptyList());
    }

    /**
     * Builds a daily plan for {@code planDate}: prefers unfinished problems in the user's focus,
     * shuffles with a deterministic seed so the same date + user gets a stable lineup that still
     * differs from other days.
     *
     * @param topicFocus optional filter; relaxed if that yields an empty pool
     */
    public LearningPlan generateDailyPersonalizedPlan(LocalDate planDate,
                                                      UUID userSalt,
                                                      String language,
                                                      int level,
                                                      Topic topicFocus,
                                                      List<UUID> completedQuestionIds) {
        if (language == null) {
            language = "";
        }
        String difficulty = mapLevelToDifficulty(level);

        List<Question> pool = selectPool(language, level, topicFocus, completedQuestionIds);

        List<PlannerStep> steps = new ArrayList<>();

        shuffleForDay(pool, planDate, userSalt, language, level, topicFocus);

        List<Question> warmup = subListSafe(pool, 0, Math.min(3, pool.size()));
        List<Question> core = subListSafe(pool, warmup.size(), Math.min(warmup.size() + 4, pool.size()));
        List<Question> stretch = subListSafe(pool, warmup.size() + core.size(),
                Math.min(warmup.size() + core.size() + 3, pool.size()));

        if (!warmup.isEmpty()) {
            steps.add(buildPhaseStep(language, difficulty, warmup, suggestedDurationMinutes(level, "warmup")));
        }

        if (!core.isEmpty()) {
            steps.add(buildPhaseStep(language, difficulty, core, suggestedDurationMinutes(level, "core")));
        }

        if (!stretch.isEmpty()) {
            steps.add(buildPhaseStep(language, difficulty, stretch, suggestedDurationMinutes(level, "stretch")));
        }

        return new LearningPlan(language, difficulty, steps);
    }

    private List<Question> selectPool(String language,
                                      int level,
                                      Topic topicFocus,
                                      List<UUID> completedIds) {
        List<Question> pool = filterPool(language, level, topicFocus, completedIds, true);
        if (pool.isEmpty() && topicFocus != null) {
            pool = filterPool(language, level, null, completedIds, true);
        }
        if (pool.isEmpty()) {
            pool = filterPool(language, level, topicFocus, completedIds, false);
            if (pool.isEmpty() && topicFocus != null) {
                pool = filterPool(language, level, null, completedIds, false);
            }
        }
        return pool;
    }

    /**
     * Questions tagged for {@code language} + level. If the catalog has no MEDIUM/HARD rows yet,
     * intermediate/advanced levels reuse the EASY pool so the planner still produces a lineup.
     */
    private ArrayList<Question> candidateQuestionsForLanguageAndLevel(String language, int level) {
        String primary = mapLevelToDifficulty(level);
        ArrayList<Question> primaryList = questionList.getByLanguageAndDifficulty(language, primary);
        if (primaryList != null && !primaryList.isEmpty()) {
            return new ArrayList<>(primaryList);
        }
        if (level >= 2 && "MEDIUM".equalsIgnoreCase(primary)) {
            ArrayList<Question> easy = questionList.getByLanguageAndDifficulty(language, "EASY");
            return easy != null ? new ArrayList<>(easy) : new ArrayList<>();
        }
        return primaryList != null ? new ArrayList<>(primaryList) : new ArrayList<>();
    }

    private List<Question> filterPool(String language,
                                      int level,
                                      Topic topicFocus,
                                      List<UUID> completedIds,
                                      boolean excludeCompleted) {
        List<Question> base = candidateQuestionsForLanguageAndLevel(language, level);
        if (base.isEmpty()) {
            return Collections.emptyList();
        }
        Set<UUID> done = new HashSet<>(completedIds != null ? completedIds : Collections.emptyList());

        ArrayList<Question> out = new ArrayList<>();
        for (Question q : base) {
            if (q == null || q.getId() == null) {
                continue;
            }
            if (excludeCompleted && done.contains(q.getId())) {
                continue;
            }
            if (topicFocus != null) {
                if (q.getTopics() == null || !q.getTopics().contains(topicFocus)) {
                    continue;
                }
            }
            out.add(q);
        }
        return out;
    }

    private void shuffleForDay(List<Question> pool,
                               LocalDate planDate,
                               UUID userSalt,
                               String language,
                               int level,
                               Topic topicFocus) {
        long seed = planDate.toEpochDay();
        seed ^= userSalt.getMostSignificantBits();
        seed ^= userSalt.getLeastSignificantBits();
        seed ^= Objects.hashCode(language);
        seed ^= (long) level * 131071L;
        seed ^= topicFocus != null ? (long) topicFocus.ordinal() * 524287L : 373587883L;
        Collections.shuffle(pool, new Random(seed));
    }

    private PlannerStep buildPhaseStep(String language,
                                       String difficulty,
                                       List<Question> questions,
                                       int durationMinutes) {
        List<UUID> ids = new ArrayList<>();
        for (Question q : questions) {
            if (q != null && q.getId() != null) {
                ids.add(q.getId());
            }
        }
        String summary = summarizeTitles(questions);
        String description = summary.isEmpty()
                ? "Problems for this block."
                : summary;
        return new PlannerStep(language, difficulty, description, durationMinutes, ids);
    }

    private static String summarizeTitles(List<Question> qs) {
        if (qs == null || qs.isEmpty()) {
            return "";
        }
        ArrayList<String> titles = new ArrayList<>();
        for (Question q : qs) {
            if (q != null && q.getTitle() != null && !q.getTitle().isBlank()) {
                titles.add(q.getTitle().trim());
            }
        }
        int maxShow = 4;
        if (titles.size() <= maxShow) {
            return String.join(", ", titles);
        }
        return String.join(", ", titles.subList(0, maxShow))
                + " +" + (titles.size() - maxShow) + " more";
    }

    private String mapLevelToDifficulty(int level) {
        return switch (level) {
            case 1 -> "EASY";
            case 2, 3 -> "MEDIUM";
            default -> "EASY";
        };
    }

    private int suggestedDurationMinutes(int level, String phase) {
        return switch (phase) {
            case "warmup" -> switch (level) {
                case 1 -> 10;
                case 2 -> 15;
                case 3 -> 20;
                default -> 10;
            };
            case "core" -> switch (level) {
                case 1 -> 15;
                case 2 -> 25;
                case 3 -> 35;
                default -> 15;
            };
            case "stretch" -> switch (level) {
                case 1 -> 10;
                case 2 -> 20;
                case 3 -> 30;
                default -> 10;
            };
            default -> 15;
        };
    }

    private static <T> List<T> subListSafe(List<T> list, int from, int to) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (from >= list.size() || from >= to) {
            return Collections.emptyList();
        }
        int end = Math.min(to, list.size());
        return new ArrayList<>(list.subList(from, end));
    }
}
