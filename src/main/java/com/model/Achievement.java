package com.model;

import java.util.UUID;

/**
 * Stores info about an achievement a user can earn.
 */
public class Achievement {

    private UUID id;
    private String name;
    private String criteria;
    private String badgeCategory;

    /**
     * Creates an empty achievement.
     */
    public Achievement() {
        this.id = UUID.randomUUID();
    }

    /**
     * Creates an achievement with fields.
     */
    public Achievement(UUID id, String name, String criteria, String badgeCategory) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.name = name;
        this.criteria = criteria;
        this.badgeCategory = badgeCategory;
    }

    /** @return the achievement's id */
    public UUID getId() {
        return id;
    }

    /** Sets the id. */
    public void setId(UUID id) {
        this.id = id;
    }

    /** @return display name of the achievement */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @return what you have to do to earn it */
    public String getCriteria() {
        return criteria;
    }

    /** Sets the criteria text. */
    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    /** @return badge group like Bronze or Silver */
    public String getBadgeCategory() {
        return badgeCategory;
    }

    /** Sets the badge category. */
    public void setBadgeCategory(String badgeCategory) {
        this.badgeCategory = badgeCategory;
    }

    /** @return a simple string with all the fields for debugging */
    @Override
    public String toString() {
        return "Achievement{id=" + id +
                ", name='" + name + '\'' +
                ", criteria='" + criteria + '\'' +
                ", badgeCategory='" + badgeCategory + '\'' +
                '}';
    }

    /**
     * Test main. Run this to see if the class works.
     */
    public static void main(String[] args) {
        Achievement a = new Achievement(
                UUID.randomUUID(),
                "First Steps",
                "Complete your first coding problem",
                "Bronze"
        );
        System.out.println("Achievement test:");
        System.out.println("  " + a);
    }
}

