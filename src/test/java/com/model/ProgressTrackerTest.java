package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Test;

/**
 * Unit tests for {@link ProgressTracker}: attempts, completion, streak rules, and null/error-safe behavior.
 * <p>
 * AI helped with boilerplate; the table is what I wanted to verify.
 * <pre>
 * +--------------------------------------+------------------------------------------------------------------+
 * | Test                                 | Reasoning                                                        |
 * +--------------------------------------+------------------------------------------------------------------+
 * | recordAttempt                        | attempt does not count as completed                              |
 * | same reference completed twice       | one completion                                                   |
 * | same id different instances          | should count once (currently fails — counts twice)               |
 * | streak rules                         | first day, same day, consecutive, gap reset                    |
 * | markCompleted updates streak         | completion drives streak                                         |
 * | resetStreak                          | clears state                                                     |
 * +--------------------------------------+------------------------------------------------------------------+
 * </pre>
 */
public class ProgressTrackerTest {

    private static Question sampleQuestion(String title) {
        return new Question(UUID.randomUUID(), title, "prompt", "EASY", new java.util.ArrayList<>(),
                new java.util.ArrayList<>(), new java.util.ArrayList<>(), UUID.randomUUID(),
                java.time.LocalDateTime.now(), "PUBLISHED");
    }

    @Test
    public void recordAttempt_doesNotMarkCompleted_whenProblemValid() {
        ProgressTracker tracker = new ProgressTracker();
        Question q = sampleQuestion("Attempt only");

        tracker.recordAttempt(q);

        assertEquals(0, tracker.getCurrentCount());
    }

    @Test
    public void markCompleted_addsProblemOnce_whenSameReferenceMarkedTwice() {
        ProgressTracker tracker = new ProgressTracker();
        Question q = sampleQuestion("Dup complete");

        tracker.markCompleted(q, 10);
        tracker.markCompleted(q, 20);

        assertEquals(1, tracker.getCurrentCount());
    }

    @Test
    public void markCompleted_addsSingleCompletion_whenSameQuestionIdOnDifferentInstances() {
        ProgressTracker tracker = new ProgressTracker();
        UUID questionId = UUID.randomUUID();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.ArrayList<com.model.enums.Topic> topics = new java.util.ArrayList<>();
        java.util.ArrayList<String> tags = new java.util.ArrayList<>();
        java.util.ArrayList<String> hints = new java.util.ArrayList<>();
        UUID author = UUID.randomUUID();

        Question fromLoader = new Question(questionId, "Two Sum", "prompt", "EASY", topics, tags, hints, author, now,
                "PUBLISHED");
        Question fromCache = new Question(questionId, "Two Sum", "prompt", "EASY", topics, tags, hints, author, now,
                "PUBLISHED");

        tracker.markCompleted(fromLoader, 60);
        tracker.markCompleted(fromCache, 30);

        assertEquals(1, tracker.getCurrentCount());
    }

    @Test
    public void updateStreak_setsStreakToOne_whenLastActiveDateWasNull() {
        ProgressTracker tracker = new ProgressTracker();
        tracker.setStreak(0);
        tracker.setLastActiveDate(null);

        tracker.updateStreak();

        assertEquals(1, tracker.getStreak());
        assertEquals(LocalDate.now(), tracker.getLastActiveDate());
    }

    @Test
    public void updateStreak_leavesStreakUnchanged_whenAlreadyActiveToday() {
        ProgressTracker tracker = new ProgressTracker();
        tracker.setStreak(4);
        tracker.setLastActiveDate(LocalDate.now());

        tracker.updateStreak();

        assertEquals(4, tracker.getStreak());
        assertEquals(LocalDate.now(), tracker.getLastActiveDate());
    }

    @Test
    public void updateStreak_incrementsStreak_whenLastActiveWasYesterday() {
        ProgressTracker tracker = new ProgressTracker();
        tracker.setStreak(3);
        tracker.setLastActiveDate(LocalDate.now().minusDays(1));

        tracker.updateStreak();

        assertEquals(4, tracker.getStreak());
        assertEquals(LocalDate.now(), tracker.getLastActiveDate());
    }

    @Test
    public void updateStreak_resetsStreakToOne_whenGapLongerThanOneDay() {
        ProgressTracker tracker = new ProgressTracker();
        tracker.setStreak(10);
        tracker.setLastActiveDate(LocalDate.now().minusDays(3));

        tracker.updateStreak();

        assertEquals(1, tracker.getStreak());
        assertEquals(LocalDate.now(), tracker.getLastActiveDate());
    }

    @Test
    public void markCompleted_updatesStreakThroughUpdateStreak() {
        ProgressTracker tracker = new ProgressTracker();
        tracker.setLastActiveDate(null);
        Question q = sampleQuestion("Streak via complete");

        tracker.markCompleted(q, 1);

        assertEquals(1, tracker.getStreak());
        assertEquals(LocalDate.now(), tracker.getLastActiveDate());
    }

    @Test
    public void resetStreak_clearsStreakAndLastActiveDate() {
        ProgressTracker tracker = new ProgressTracker();
        tracker.setStreak(5);
        tracker.setLastActiveDate(LocalDate.now());

        tracker.resetStreak();

        assertEquals(0, tracker.getStreak());
        assertNull(tracker.getLastActiveDate());
    }
}
