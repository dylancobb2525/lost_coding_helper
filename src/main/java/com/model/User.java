package com.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * This represents a User which stores their information and progress.
 * The class is abstract and allows for more specific types of users
 */
public abstract class User {
    protected UUID userId;
    protected Date joinDate;

    private String displayName;
    private String accountId;
    private String email;
    private String username;
    private String hashedPassword;
    private boolean isLocked;
    private int failedLoginCount;
    private LocalDateTime lastFailedLoginAt;
    private ArrayList<UUID> achievements;
    private ArrayList<UUID> favoritedProblems; //UUID for JSON
    private ArrayList<Question> favoriteProblems; //Question list
    private ProgressTracker progressTracker;
    private UUID progressTrackerId;

    private static final int MAX_FAILED_LOGINS = 5;

    public User(UUID userId, String displayName, String accountId, String email, String username, String hashedPassword) {
        this.userId = (userId != null) ? userId : UUID.randomUUID();
        this.joinDate = new Date();

        this.displayName = displayName;
        this.accountId = accountId;
        this.email = email;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.isLocked = false;
        this.failedLoginCount = 0;
        this.lastFailedLoginAt = null;
        this.achievements = new ArrayList<>();
        this.favoritedProblems = new ArrayList<>();
        this.favoriteProblems = new ArrayList<>();
        this.progressTracker = new ProgressTracker();
        this.progressTrackerId = this.userId;
    }

    public UUID getUserId() {
    return userId;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public LocalDateTime getLastFailedLoginAt() {
        return lastFailedLoginAt;
    }

    public int getStreak() {
        return progressTracker.getStreak();
    }

    public LocalDate getLastActiveDate() {
        return progressTracker.getLastActiveDate();
    }

    public ArrayList<Question> getFavoriteProblems() {
        return favoriteProblems;
    }

    public ArrayList<UUID> getFavoritedProblemIds() {
        return favoritedProblems;
    }

    /**
     * Replaces favorites with the given list (null argument = no-op).
     * Skips null questions or questions with null id; deduplicates by question id.
     * Keeps {@link #favoritedProblems} in sync for JSON / id-based use.
     */
    public void setFavoriteProblems(ArrayList<Question> favorites) {
        if (favorites == null) {
            return;
        }
        favoriteProblems.clear();
        favoritedProblems.clear();
        for (Question q : favorites) {
            if (q == null || q.getId() == null) {
                continue;
            }
            UUID id = q.getId();
            boolean already = false;
            for (Question existing : favoriteProblems) {
                if (existing != null && id.equals(existing.getId())) {
                    already = true;
                    break;
                }
            }
            if (!already) {
                favoriteProblems.add(q);
                favoritedProblems.add(id);
            }
        }
    }

    public ArrayList<UUID> getAchievementIds() {
        return achievements;
    }

    public UUID getProgressTrackerId() {
        return progressTrackerId;
    }

    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }


    public abstract boolean hasAccess(String feature);

    public abstract boolean canSubmitSolutions();

    public abstract boolean canTrackProgress();

    public abstract boolean canCreateProblems();

    public abstract boolean canViewMultipleHints();

    public abstract boolean canFavoriteProblems();
    

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        if (name == null) return;
        String trimmedName = name.trim();
        if (trimmedName.length() >= 3 && trimmedName.length() <= 25) {
            this.displayName = trimmedName;
        }
    }

    /**
     * This validates a login by checking the username and password against stored values.
     * It will also check if the account is locked and will increment failed login attempts if the credentials are incorrect.
     * @param username
     * @param password
     * @return
     */
    public boolean validateCredentials(String username, String password) {
        if (isLocked) {
            return false;
        }

        if (username == null || password == null) {
            incrementFailedLogin();
            return false;
        }

        if (this.username == null || !this.username.equalsIgnoreCase(username.trim())) {
            incrementFailedLogin();
            return false;
        }

        boolean match = ((hashedPassword != null) && hashedPassword.equals(password));
        if (match) {
            resetFailedLogin();
            return true;
        } else {
            incrementFailedLogin();
            return false;
        }
    }

    public void incrementFailedLogin() {
        failedLoginCount++;
        lastFailedLoginAt = LocalDateTime.now();

        if (failedLoginCount >= MAX_FAILED_LOGINS) {
            lockAccount();
        }
    }

    public void resetFailedLogin() {
        this.failedLoginCount = 0;
        this.lastFailedLoginAt = null;
    }

    public void lockAccount() {
        isLocked = true;
    }

    public void unlockAccount() {
        isLocked = false;
        resetFailedLogin();
    }

    /**
     * changes the password if the old password is correct and new password meets all the requirements.
     * @param oldPassword
     * @param newPassword
     */
    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null) return;

        if (hashedPassword == null || !hashedPassword.equals(oldPassword)) {
            return;
        }

        if (!validatePassword(newPassword)) {
            return;
        }

        hashedPassword = newPassword;
        resetFailedLogin();
    }

    public boolean validateEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty()) return false;

        int atIndex = e.indexOf('@');
        int dotIndex = e.lastIndexOf('.');
        return atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < e.length() - 1;
    }

    public boolean validateUsername(String username) {
        if (username == null) return false;
        String user = username.trim();
        if (user.length() < 3 || user.length() > 25) return false;

        return user.matches("[a-zA-Z0-9_]+"); 
    }

    public boolean validatePassword(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        return hasUpper && hasLower && hasDigit;
    }

    public boolean isUsernameUnique(String username) { 
        return true;
    }




}
