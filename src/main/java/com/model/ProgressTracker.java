package com.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.model.enums.ActivityType;


/**
 * Tracks user progress such as completed problems, activities, and streaks. 
 * Used to keep record of a users progress over time.
 */
public class ProgressTracker {
    private List<Question> completedProblems;
    private ArrayList<String> userActivities;
    private int streak;
    private LocalDate lastActiveDate;

    public ProgressTracker() {
        completedProblems = new ArrayList<>();
        userActivities = new ArrayList<>();
        streak = 0;
        lastActiveDate = null;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void setLastActiveDate(LocalDate lastActiveDate) {
        this.lastActiveDate = lastActiveDate;
    }

    public int getStreak() {
        return streak;
    }

    public LocalDate getLastActiveDate() {
        return lastActiveDate;
    }

    public void resetStreak() {
        streak = 0;
        lastActiveDate = null;
    }

    public void recordAttempt(Question problem) {
        if (problem == null) {
            return;
        }
        logActivity(ActivityType.ATTEMPT, "Attempted problem: " + problem.getTitle());
    }

    public void markCompleted(Question problem, int timeSpentSec) {
        if (problem == null) {
            return;
        }
        if (!completedProblems.contains(problem)) {
            completedProblems.add(problem);
        }
        logActivity(ActivityType.COMPLETE, "Completed problem: " + problem.getTitle() + " in " + timeSpentSec + " seconds");
        updateStreak();
    }

    /**
     * adds new activity to the user activity. Combines activity type and details for a log entry.
     * @param activityType
     * @param details
     */
    public void logActivity(ActivityType activityType, String details) {
        if (activityType == null || details == null) {
            return;
        }
        String activity = activityType.toString() + ": " + details;
        userActivities.add(activity);
    } 

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

    public ArrayList<Question> getCompletedQuestionsByDifficulty() {
        return new ArrayList<>(completedProblems);
    }

    public ArrayList<Question> getCompletedQuestionsByTopic() {
        // returns all completed questions needs a parameter for topic
        return new ArrayList<>(completedProblems);
    }

    public int getCurrentCount() {
        return completedProblems.size();
    }

    public void addActivity() {
        
        logActivity(ActivityType.OTHER, "User activity recorded");
    }






}
