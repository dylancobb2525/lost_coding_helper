package com.model;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class PlannerStepTest {

    private List<UUID> questionIds;

    @Before
    public void setUp() {
        questionIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void constructor_storesPassedValuesCorrectly() {
        PlannerStep step = new PlannerStep("Java", "Easy", "Practice loops", 20, questionIds);

        assertEquals("Java", step.getLanguage());
        assertEquals("Easy", step.getDifficulty());
        assertEquals("Practice loops", step.getDescription());
        assertEquals(20, step.getDurationMinutes());
        assertEquals(questionIds, step.getQuestionIds());
    }

    /*
     * This class tests that PlannerStep stores the constructor values correctly,
     * including language, difficulty, description, duration, and question ids.
     */
}