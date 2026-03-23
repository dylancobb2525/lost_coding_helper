package com.model;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
/**
 * Handles a list of users and provides leaderboard features
 * like top steaks or days used
 * All rankings are based on User.getStreak().
 */
public class LeaderBoard {
    
    /** List of users in the leaderboard */
    private List<User> users = new ArrayList<>();
    /**
     * Creates a leaderboard with a copy of the given users.
     * If null, an empty list is used.
     * @param users list of users
     */
    public LeaderBoard(List<User> users) {
        // Defensive copy so caller mutations don't affect leaderboard; null -> empty list.
        this.users = users == null ? new ArrayList<>() : new ArrayList<>(users);
    }
    /**
     * Gets the top users by streak (highest first).
     * @param limit max number of users to return
     * @return list of top users
     */
    public List<User> getTopPerformers(int limit) {
        long safeLimit = Math.max(0, limit);
        return users.stream()
                .sorted((a, b) -> b.getStreak() - a.getStreak())
                .limit(safeLimit)
                .toList();
    }
    
     /**
     * Gets basic stats about users.
     * Keys:
     * totalUsers, highestScore, averageScore
     * @return stats map
     */
    public HashMap<String, Integer> getStats() {
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        // Keys say "Score" for compatibility; values are login/activity streaks from User.getStreak().
        stats.put("highestScore", users.stream().mapToInt(User::getStreak).max().orElse(0));
        stats.put("averageScore", (int) users.stream().mapToInt(User::getStreak).average().orElse(0));
        return stats;
    }
}
