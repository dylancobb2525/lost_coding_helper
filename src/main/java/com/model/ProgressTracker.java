package com.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.model.enums.ActivityType;

public class ProgressTracker {
    private List<Question> completedProblems;
    private ArrayList<String> userActivities;

    public ProgressTracker() {
        completedProblems = new ArrayList<>();
        userActivities = new ArrayList<>();
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
        // Check if already completed to avoid duplicates
        if (!completedProblems.contains(problem)) {
            completedProblems.add(problem);
        }
        logActivity(ActivityType.COMPLETE, "Completed problem: " + problem.getTitle() + " in " + timeSpentSec + " seconds");
    }

    public void logActivity(ActivityType activityType, String details) {
        if (activityType == null || details == null) {
            return;
        }
        String activity = activityType.toString() + ": " + details;
        userActivities.add(activity);
    } 

    public void updateStreak(Date activityDate) {
        
        // placeholder for strak logic in user 
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
