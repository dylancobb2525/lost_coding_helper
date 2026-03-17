package com.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Builds a study plan (sequence of PlannerStep) for a given language and skill level.
 * This stays purely backend and does not interact with UI or persistence.
 */
public class StudyPlanner {

    private final QuestionList questionList;

    public StudyPlanner(QuestionList questionList) {
        this.questionList = questionList;
    }

    /**
     * level: 1 = beginner, 2 = intermediate, 3 = advanced (conceptually).
     * Currently all map onto the underlying "EASY" difficulty string used
     * by Question in this project.
     * language: e.g. "Java", "C++", "Python".
     */
    public LearningPlan generatePlan(String language, int level) {
        if (language == null) {
            language = "";
        }

        String difficulty = mapLevelToDifficulty(level);

        List<PlannerStep> steps = new ArrayList<>();

        List<Question> selected = selectQuestionsForLevel(language, difficulty, level);

        List<Question> warmup = subListSafe(selected, 0, Math.min(3, selected.size()));
        List<Question> core = subListSafe(selected, warmup.size(), Math.min(warmup.size() + 4, selected.size()));
        List<Question> stretch = subListSafe(selected, warmup.size() + core.size(),
                Math.min(warmup.size() + core.size() + 3, selected.size()));

        if (!warmup.isEmpty()) {
            steps.add(buildStep(
                    language,
                    difficulty,
                    "Start with easier " + language + " questions to warm up.",
                    suggestedDurationMinutes(level, "warmup"),
                    warmup
            ));
        }

        if (!core.isEmpty()) {
            steps.add(buildStep(
                    language,
                    difficulty,
                    "Focus on core " + language + " questions at your level.",
                    suggestedDurationMinutes(level, "core"),
                    core
            ));
        }

        if (!stretch.isEmpty()) {
            steps.add(buildStep(
                    language,
                    difficulty,
                    "Try some stretch " + language + " questions that are a bit harder.",
                    suggestedDurationMinutes(level, "stretch"),
                    stretch
            ));
        }

        return new LearningPlan(language, difficulty, steps);
    }

    private String mapLevelToDifficulty(int level) {
        // Align with existing data, which currently uses "EASY" as difficulty.
        // All levels map to "EASY" for now so planner can find questions.
        return "EASY";
    }

    private List<Question> selectQuestionsForLevel(String language, String difficulty, int level) {
        // Since we only have "EASY" difficulty at the moment, just use that.
        return questionList.getByLanguageAndDifficulty(language, difficulty);
    }

    private PlannerStep buildStep(String language,
                                  String difficulty,
                                  String description,
                                  int durationMinutes,
                                  List<Question> questions) {

        List<UUID> ids = new ArrayList<>();
        for (Question q : questions) {
            if (q != null && q.getId() != null) {
                ids.add(q.getId());
            }
        }

        return new PlannerStep(language, difficulty, description, durationMinutes, ids);
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

    private <T> List<T> subListSafe(List<T> list, int from, int to) {
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

