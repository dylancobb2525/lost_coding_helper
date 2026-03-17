package com.model;

import java.util.List;
import java.util.UUID;

/**
 * Represents a single step in a study plan, such as
 * "Work on beginner Java loop questions for 15 minutes."
 */
public class PlannerStep {
    private final String language;
    private final String difficulty;
    private final String description;
    private final int durationMinutes;
    private final List<UUID> questionIds;

    public PlannerStep(String language,
                       String difficulty,
                       String description,
                       int durationMinutes,
                       List<UUID> questionIds) {
        this.language = language;
        this.difficulty = difficulty;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.questionIds = questionIds;
    }

    public String getLanguage() {
        return language;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public List<UUID> getQuestionIds() {
        return questionIds;
    }
}

