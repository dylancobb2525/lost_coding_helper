package com.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.lost_coding_helper.App;
import com.model.Question;
import com.model.User;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProgressController {

    @FXML private Label streakValueLabel;
    @FXML private Label solvedValueLabel;
    @FXML private Label badgesValueLabel;

    @FXML private FlowPane badgesPane;
    @FXML private VBox completedQuestionsList;
    @FXML private VBox favoriteQuestionsList;

    private static final String BADGE_IMAGE_PREFIX = "/com/lost_coding_helper/badge_icons/";

    @FXML
    private void initialize() {
        setProgress();
    }

    private void setProgress() {
        User user = App.getApplication() != null ? App.getApplication().getCurrentUser() : null;

        if (user == null) {
            setGuestState();
            return;
        }

        int streakCount = user.getStreak();
        int solvedCount = user.getProgressTracker() != null
                ? user.getProgressTracker().getCurrentCount()
                : 0;
        int achievementCount = user.getAchievementIds() != null
                ? user.getAchievementIds().size()
                : 0;

        ArrayList<BadgeInfo> earnedBadges = getEarnedBadges(streakCount, solvedCount, achievementCount);

        if (streakValueLabel != null) {
            streakValueLabel.setText(String.valueOf(streakCount));
        }
        if (solvedValueLabel != null) {
            solvedValueLabel.setText(String.valueOf(solvedCount));
        }
        if (badgesValueLabel != null) {
            badgesValueLabel.setText(String.valueOf(earnedBadges.size()));
        }

        setBadges(earnedBadges);
        setCompletedQuestions(user);
        setFavoriteQuestions(user);
    }

    private ArrayList<BadgeInfo> getEarnedBadges(int streakCount, int solvedCount, int achievementCount) {
        ArrayList<BadgeInfo> badges = new ArrayList<>();

        addBadgeIfEarned(badges, streakCount, 10, "10 Day Streak", "streak_10_badge.png");
        addBadgeIfEarned(badges, streakCount, 50, "50 Day Streak", "streak_50_badge.png");
        addBadgeIfEarned(badges, streakCount, 100, "100 Day Streak", "streak_100_badge.png");

        addBadgeIfEarned(badges, solvedCount, 10, "10 Solved", "solved_10_badge.png");
        addBadgeIfEarned(badges, solvedCount, 50, "50 Solved", "solved_50_badge.png");
        addBadgeIfEarned(badges, solvedCount, 100, "100 Solved", "solved_100_badge.png");

        addBadgeIfEarned(badges, achievementCount, 5, "5 Achievements", "achievement_5_badge.png");
        addBadgeIfEarned(badges, achievementCount, 10, "10 Achievements", "achievement_10_badge.png");
        addBadgeIfEarned(badges, achievementCount, 20, "20 Achievements", "achievement_20_badge.png");
        
        return badges;
    }

    private void addBadgeIfEarned(ArrayList<BadgeInfo> badges, int currentValue, int targetValue, String label, String fileName) {
        if (currentValue >= targetValue) {
            badges.add(new BadgeInfo(label, fileName));
        }
    }

    private void setGuestState() {
        if (streakValueLabel != null) {
            streakValueLabel.setText("0");
        }
        if (solvedValueLabel != null) {
            solvedValueLabel.setText("0");
        }
        if (badgesValueLabel != null) {
            badgesValueLabel.setText("0");
        }

        if (badgesPane != null) {
            badgesPane.getChildren().clear();
            badgesPane.getChildren().add(makeMutedLabel("Log in to earn badges"));
        }

        if (completedQuestionsList != null) {
            completedQuestionsList.getChildren().clear();
            completedQuestionsList.getChildren().add(makeListItem("No completed questions yet"));
        }

        if (favoriteQuestionsList != null) {
            favoriteQuestionsList.getChildren().clear();
            favoriteQuestionsList.getChildren().add(makeListItem("No favorite questions yet"));
        }
    }

    private void setBadges(List<BadgeInfo> earnedBadges) {
        if (badgesPane == null) {
            return;
        }

        badgesPane.getChildren().clear();

        if (earnedBadges == null || earnedBadges.isEmpty()) {
            badgesPane.getChildren().add(makeMutedLabel("No badges earned yet"));
            return;
        }

        for (BadgeInfo badge : earnedBadges) {
            badgesPane.getChildren().add(makeBadgeItem(badge));
        }
    }

    private VBox makeBadgeItem(BadgeInfo badge) {
        VBox badgeBox = new VBox(6);
        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.getStyleClass().add("badge-item");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(42);
        imageView.setFitHeight(42);
        imageView.setPreserveRatio(true);

        URL url = ProgressController.class.getResource(BADGE_IMAGE_PREFIX + badge.fileName);
        if (url != null) {
            imageView.setImage(new Image(url.toExternalForm(), true));
        }

        Label label = new Label(badge.label);
        label.getStyleClass().add("badge-label");
        label.setWrapText(true);

        badgeBox.getChildren().addAll(imageView, label);
        return badgeBox;
    }

    private void setCompletedQuestions(User user) {
        if (completedQuestionsList == null) {
            return;
        }

        completedQuestionsList.getChildren().clear();

        ArrayList<Question> completed = user.getProgressTracker() != null
                ? user.getProgressTracker().getCompletedQuestionsByDifficulty()
                : new ArrayList<>();

        if (completed == null || completed.isEmpty()) {
            completedQuestionsList.getChildren().add(makeListItem("No completed questions yet"));
            return;
        }

        addQuestionTitles(completedQuestionsList, completed, 3, "No completed questions yet");
    }

    private void setFavoriteQuestions(User user) {
        if (favoriteQuestionsList == null) {
            return;
        }

        favoriteQuestionsList.getChildren().clear();

        List<Question> favorites = user.getFavoriteProblems();
        if (favorites == null || favorites.isEmpty()) {
            favoriteQuestionsList.getChildren().add(makeListItem("No favorite questions yet"));
            return;
        }

        addQuestionTitles(favoriteQuestionsList, favorites, 3, "No favorite questions yet");
    }

    private void addQuestionTitles(VBox container, List<Question> questions, int limit, String fallback) {
        int shown = 0;

        for (Question question : questions) {
            if (question == null || question.getTitle() == null || question.getTitle().isBlank()) {
                continue;
            }

            container.getChildren().add(makeListItem(question.getTitle()));
            shown++;

            if (shown >= limit) {
                break;
            }
        }

        if (shown == 0) {
            container.getChildren().add(makeListItem(fallback));
        }
    }

    private HBox makeListItem(String text) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("progress-list-item");

        Label label = new Label(text);
        label.getStyleClass().add("progress-list-label");

        row.getChildren().add(label);
        return row;
    }

    private Label makeMutedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("favorites-item-muted");
        return label;
    }

    @FXML
    private void openLeaderboard() throws IOException {
        App.setRoot("leaderboard");
    }

    @FXML
    private void navProgress() throws IOException {
        App.setRoot("progress");
    }

    @FXML
    private void navHome() throws IOException {
        App.setRoot("home");
    }

    @FXML
    private void navCalendar() throws IOException {
        App.setRoot("calendar");
    }

    @FXML
    private void navLeaderboard() throws IOException {
        App.setRoot("leaderboard");
    }

    @FXML
    private void navHelp() throws IOException {
        App.setRoot("help");
    }

    @FXML
    private void navProfile() throws IOException {
        App.setRoot("profile");
    }

    private static class BadgeInfo {
        private final String label;
        private final String fileName;

        private BadgeInfo(String label, String fileName) {
            this.label = label;
            this.fileName = fileName;
        }
    }
}