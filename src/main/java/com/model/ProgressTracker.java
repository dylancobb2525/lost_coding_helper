package com.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.model.enums.ActivityType;

/**
 * Tracks completed problems, activity log, and daily streak for a user.
 *
 * @author Christopher Feuchter
 */
public class ProgressTracker {
    private List<Question> completedProblems;
    private ArrayList<String> userActivities;
    private int streak;
    private LocalDate lastActiveDate;
    /** Calendar day for which {@link #completionsToday} applies; reset when the day changes. */
    private LocalDate completionsDay;
    private int completionsToday;

    /** Creates an empty tracker with streak 0. */
    public ProgressTracker() {
        completedProblems = new ArrayList<>();
        userActivities = new ArrayList<>();
        streak = 0;
        lastActiveDate = null;
    }

    /**
     * @param streak consecutive active days
     */
    public void setStreak(int streak) {
        this.streak = streak;
    }

    /**
     * @param lastActiveDate last day activity was recorded
     */
    public void setLastActiveDate(LocalDate lastActiveDate) {
        this.lastActiveDate = lastActiveDate;
    }

    /**
     * @return consecutive active days
     */
    public int getStreak() {
        return streak;
    }

    /**
     * @return last activity date, or {@code null}
     */
    public LocalDate getLastActiveDate() {
        return lastActiveDate;
    }

    /** Sets streak to 0 and clears the last activity date. */
    public void resetStreak() {
        streak = 0;
        lastActiveDate = null;
    }

    /**
     * Logs an attempt for the given problem.
     *
     * @param problem the problem; ignored if {@code null}
     */
    public void recordAttempt(Question problem) {
        if (problem == null) {
            return;
        }
        logActivity(ActivityType.ATTEMPT, "Attempted problem: " + problem.getTitle());
    }

    /**
     * Marks a problem completed, updates the activity log, and refreshes the streak.
     *
     * @param problem the problem; ignored if {@code null}
     * @param timeSpentSec time spent in seconds
     */
    public void markCompleted(Question problem, int timeSpentSec) {
        if (problem == null) {
            return;
        }
        boolean newlyCompleted = !hasCompletedId(problem);
        if (newlyCompleted) {
            completedProblems.add(problem);
            bumpCompletionsToday();
        }
        logActivity(ActivityType.COMPLETE, "Completed problem: " + problem.getTitle() + " in " + timeSpentSec + " seconds");
        updateStreak();
    }

    private void bumpCompletionsToday() {
        LocalDate today = LocalDate.now();
        if (completionsDay == null || !completionsDay.equals(today)) {
            completionsDay = today;
            completionsToday = 0;
        }
        completionsToday++;
    }

    /**
     * How many problems were marked completed today (resets at local midnight in memory).
     */
    public int getCompletionsToday() {
        LocalDate today = LocalDate.now();
        if (completionsDay == null || !completionsDay.equals(today)) {
            return 0;
        }
        return completionsToday;
    }

    /** Calendar day that {@link #getCompletionsTodayRaw()} refers to; may be null. */
    public LocalDate getCompletionsDay() {
        return completionsDay;
    }

    /** Stored count for {@link #completionsDay} (use with day check). */
    public int getCompletionsTodayRaw() {
        return completionsToday;
    }

    /**
     * Restores today's completion count from JSON. Ignores stale days.
     */
    public void restoreCompletionsForDay(LocalDate day, int count) {
        LocalDate today = LocalDate.now();
        if (day != null && day.equals(today) && count >= 0) {
            this.completionsDay = day;
            this.completionsToday = count;
        } else {
            this.completionsDay = null;
            this.completionsToday = 0;
        }
    }

    /**
     * Appends a formatted line to the activity log.
     *
     * @param activityType kind of activity
     * @param details human-readable detail text
     */
    public void logActivity(ActivityType activityType, String details) {
        if (activityType == null || details == null) {
            return;
        }
        String activity = activityType.toString() + ": " + details;
        userActivities.add(activity);
    }

    /** Updates streak based on {@link #lastActiveDate} and today. */
    public void updateStreak() {
        LocalDate today = LocalDate.now();

        if (lastActiveDate == null) {
            streak = 1;
        } else if (lastActiveDate.isEqual(today)) {
            return;
        } else if (lastActiveDate.isEqual(today.minusDays(1))) {
            streak++;
        } else {
            streak = 1;
        }
        lastActiveDate = today;
    }

    /**
     * @return a copy of all completed problems
     */
    public ArrayList<Question> getCompletedQuestionsByDifficulty() {
        return new ArrayList<>(completedProblems);
    }

    /**
     * @return a copy of all completed problems (topic filtering not yet implemented)
     */
    public ArrayList<Question> getCompletedQuestionsByTopic() {
        return new ArrayList<>(completedProblems);
    }

    /**
     * @return number of completed problems
     */
    public int getCurrentCount() {
        return completedProblems.size();
    }

    /** Logs a generic activity entry. */
    public void addActivity() {
        logActivity(ActivityType.OTHER, "User activity recorded");
    }

    private boolean hasCompletedId(Question problem) {
        if (problem == null || problem.getId() == null) {
            return false;
        }
        for (Question p : completedProblems) {
            if (p != null && problem.getId().equals(p.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces the in-memory completed list (used when loading from JSON).
     */
    public void replaceCompletedList(List<Question> questions) {
        completedProblems.clear();
        if (questions == null) {
            return;
        }
        for (Question q : questions) {
            if (q != null) {
                completedProblems.add(q);
            }
        }
    }
}
