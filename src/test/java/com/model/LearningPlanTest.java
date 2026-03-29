package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class LearningPlanTest {

    private List<PlannerStep> steps;
    private PlannerStep stepOne;
    private PlannerStep stepTwo;

    @Before
    public void setUp() {
        stepOne = new PlannerStep("Java", "Easy", "Practice loops", 15, List.of(UUID.randomUUID()));
        stepTwo = new PlannerStep("Java", "Easy", "Practice arrays", 20, List.of(UUID.randomUUID()));

        steps = new ArrayList<>();
        steps.add(stepOne);
        steps.add(stepTwo);
    }

    @Test
    public void constructor_usesEmptyListWhenStepsNull() {
        LearningPlan plan = new LearningPlan("Java", "Easy", null);

        assertNotNull(plan.getSteps());
        assertEquals(0, plan.getSteps().size());
    }

    @Test
    public void constructor_copiesInputList() {
        LearningPlan plan = new LearningPlan("Java", "Easy", steps);
        steps.add(new PlannerStep("Java", "Medium", "Practice methods", 25, List.of(UUID.randomUUID())));

        assertEquals(2, plan.getSteps().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSteps_returnsUnmodifiableList() {
        LearningPlan plan = new LearningPlan("Java", "Easy", steps);

        plan.getSteps().add(new PlannerStep("Java", "Hard", "Practice recursion", 30, List.of(UUID.randomUUID())));
    }

    @Test
    public void getTotalDurationMinutes_addsAllStepTimes() {
        LearningPlan plan = new LearningPlan("Java", "Easy", steps);

        assertEquals(35, plan.getTotalDurationMinutes());
    }

    @Test
    public void getTotalDurationMinutes_returnsZeroWhenNoSteps() {
        LearningPlan plan = new LearningPlan("Java", "Easy", new ArrayList<PlannerStep>());

        assertEquals(0, plan.getTotalDurationMinutes());
    }

    /*
     * Simple summary for our group / teacher:
     * This class tests LearningPlan list handling and total time calculation. We check null step
     * lists, copied lists, protected step lists, and total duration across all planner steps.
     */
}
