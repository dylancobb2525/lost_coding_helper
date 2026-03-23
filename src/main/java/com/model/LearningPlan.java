package com.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A full study plan for a given language and difficulty, made up of steps.
 */
public class LearningPlan {
    private final String language;
    private final String difficulty;
    private final List<PlannerStep> steps;

    /**
     * Builds a study plan for the given language and difficulty. Copies the steps list so it can't be changed from outside.
     */
    public LearningPlan(String language, String difficulty, List<PlannerStep> steps) {
        this.language = language;
        this.difficulty = difficulty;
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
    }

    /**
     * Returns the language this plan is for (e.g. Java, C++).
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the difficulty level (EASY, MEDIUM, etc.).
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * Returns the list of steps in this plan. The list is unmodifiable so nobody can change it.
     */
    public List<PlannerStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Adds up the duration of all steps and returns the total in minutes.
     */
    public int getTotalDurationMinutes() {
        int total = 0;
        for (PlannerStep step : steps) {
            total += step.getDurationMinutes();
        }
        return total;
    }
}

