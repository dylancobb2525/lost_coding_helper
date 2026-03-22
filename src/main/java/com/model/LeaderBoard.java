package com.model;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class LeaderBoard {

    private List<User> users = new ArrayList<>();

    public LeaderBoard(List<User> users) {
        // Defensive copy so caller mutations don't affect leaderboard; null -> empty list.
        this.users = users == null ? new ArrayList<>() : new ArrayList<>(users);
    }

    public List<User> getTopPerformers(int limit) {
        long safeLimit = Math.max(0, limit);
        return users.stream()
                .sorted((a, b) -> b.getStreak() - a.getStreak())
                .limit(safeLimit)
                .toList();
    }

    public HashMap<String, Integer> getStats() {
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        // Keys say "Score" for compatibility; values are login/activity streaks from User.getStreak().
        stats.put("highestScore", users.stream().mapToInt(User::getStreak).max().orElse(0));
        stats.put("averageScore", (int) users.stream().mapToInt(User::getStreak).average().orElse(0));
        return stats;
    }
}
