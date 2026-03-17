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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getBadgeCategory() {
        return badgeCategory;
    }

    public void setBadgeCategory(String badgeCategory) {
        this.badgeCategory = badgeCategory;
    }

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

