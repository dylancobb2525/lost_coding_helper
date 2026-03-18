package com.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

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
    private int streak;
    private LocalDate lastActiveDate;
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
        this.streak = 0;
        this.lastActiveDate = null;
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
        return streak;
    }

    public LocalDate getLastActiveDate() {
        return lastActiveDate;
    }

    public ArrayList<Question> getFavoriteProblems() {
        return favoriteProblems;
    }

    public ArrayList<UUID> getFavoritedProblemIds() {
        return favoritedProblems;
    }

    public void setFavoriteProblems(ArrayList<Question> favorites) {
        if (favoriteProblems != null) {
            for (Question q : favorites){
                if (q != null && !this.favoriteProblems.contains(q)){
                    this.favoriteProblems.add(q);
                }
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
            //we can add streak if we want for just logging in if so increaseStreak();
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

    public boolean isUsernameUnique(String username) { //should be done in user list
        return true;
    }

    public void increaseStreak() {
        LocalDate today = LocalDate.now();

        if (lastActiveDate == null) {
            streak = 1;
        } else if (lastActiveDate.isEqual(today)) {
            //does nothing
        } else if (lastActiveDate.isEqual(today.minusDays(1))) {
            streak++;
        } else {
            streak = 1;
        }

        lastActiveDate = today;
    }

    public void resetStreak() {
        streak = 0;
    }










}
