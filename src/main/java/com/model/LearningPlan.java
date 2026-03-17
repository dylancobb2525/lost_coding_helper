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

    public LearningPlan(String language, String difficulty, List<PlannerStep> steps) {
        this.language = language;
        this.difficulty = difficulty;
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
    }

    public String getLanguage() {
        return language;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<PlannerStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public int getTotalDurationMinutes() {
        int total = 0;
        for (PlannerStep step : steps) {
            total += step.getDurationMinutes();
        }
        return total;
    }
}

