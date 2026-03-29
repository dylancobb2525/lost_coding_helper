package com.model;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class AchievementTest {

    private UUID id;

    @Before
    public void setUp() {
        id = UUID.randomUUID();
    }

    @Test
    public void defaultConstructor_createsNonNullId() {
        Achievement achievement = new Achievement();

        assertNotNull(achievement.getId());
    }

    @Test
    public void fullConstructor_keepsProvidedId() {
        Achievement achievement = new Achievement(id, "First Steps", "Complete one problem", "Bronze");

        assertEquals(id, achievement.getId());
    }

    @Test
    public void fullConstructor_createsNewIdWhenPassedNull() {
        Achievement achievement = new Achievement(null, "First Steps", "Complete one problem", "Bronze");

        assertNotNull(achievement.getId());
    }

    @Test
    public void toString_containsImportantFieldValues() {
        Achievement achievement = new Achievement(id, "First Steps", "Complete one problem", "Bronze");

        String result = achievement.toString();

        assertTrue(result.contains(id.toString()));
        assertTrue(result.contains("First Steps"));
        assertTrue(result.contains("Complete one problem"));
        assertTrue(result.contains("Bronze"));
    }

    /*
     * This class tests Achievement object creation. We check that ids are made when needed,
     * that the constructor keeps a provided id, and that toString includes the important fields.
     */
}
