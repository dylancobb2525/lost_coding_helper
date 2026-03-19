package com.model;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class LeaderBoard {

    private List<User> users = new ArrayList<>();

    public LeaderBoard(List<User> users) {
        this.users = users;
    }

    public List<User> getTopPerformers(int limit) {
        return users.stream()
                .sorted((a, b) -> b.getStreak() - a.getStreak())
                .limit(limit)
                .toList();
    }

    public HashMap<String, Integer> getStats() {
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("highestScore", users.stream().mapToInt(User::getStreak).max().orElse(0));
        stats.put("averageScore",(int) users.stream().mapToInt(User::getStreak).average().orElse(0));

         return stats;
    }
}
