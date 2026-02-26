package com.lost_coding_helper;

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
    private ArrayList<Achievement> achievements;
    private int streak;
    private LocalDate lastActiveDate;
    private ArrayList<Question> favoriteProblems;
    private ProgressTracker progressTracker;

    public User(UUID userId, String displayName, String accountId, String email, String username, String hashedPassword) {

    }

    public String getUserId() {
    return "";
    }

    public Date getJoinDate() {
        return null;
    }

    public abstract boolean hasAccess(String feature);

    public abstract boolean canSubmitSolutions();

    public abstract boolean canTrackProgress();

    public abstract boolean canCreateProblems();

    public abstract boolean canViewMultipleHints();

    public abstract boolean canFavoriteProblems();
    

    public String getDisplayName() {
        return null;
    }

    public void setDisplayName(String name) {
        
    }

    public boolean validateCredentials(String username, String password) {
        return false;
    }

    public void incrementFailedLogin() {

    }

    public void resetFailedLogin() {

    }

    public void lockAccount() {

    }

    public void unlockAccount() {

    }

    public void changePassword(String oldPassword, String newPassword) {

    }

    public boolean validateEmail(String email) {
        return false;
    }

    public boolean validateUsername(String username) {
        return false;
    }

    public boolean validatePassword(String password) {
        return false;
    }

    public boolean isUsernameUnique(String username) {
        return false;
    }

    public void increaseStreak() {

    }

    public void resetStreak() {

    }










}
