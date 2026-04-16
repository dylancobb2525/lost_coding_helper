package com.model;

import java.util.UUID;

/**
 * Ephemeral guest user for sessions that do not require account registration.
 */
public class Guest extends User {
    private static final String GUEST_DISPLAY_NAME = "Guest";
    private static final String GUEST_ACCOUNT_PREFIX = "guest-";
    private static final String GUEST_EMAIL_DOMAIN = "guest.local";
    private static final String GUEST_USERNAME = "guest";

    public Guest() {
        super(
                UUID.randomUUID(),
                GUEST_DISPLAY_NAME,
                GUEST_ACCOUNT_PREFIX + UUID.randomUUID(),
                GUEST_USERNAME + "@" + GUEST_EMAIL_DOMAIN,
                GUEST_USERNAME,
                ""
        );
    }

    @Override
    public boolean hasAccess(String feature) {
        if (feature == null || feature.isBlank()) {
            return false;
        }
        String normalized = feature.trim().toLowerCase();
        return normalized.contains("hint");
    }

    @Override
    public boolean canSubmitSolutions() {
        return false;
    }

    @Override
    public boolean canTrackProgress() {
        return false;
    }

    @Override
    public boolean canCreateProblems() {
        return false;
    }

    @Override
    public boolean canViewMultipleHints() {
        return true;
    }

    @Override
    public boolean canFavoriteProblems() {
        return false;
    }
}
