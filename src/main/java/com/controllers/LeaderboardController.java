package com.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.User;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LeaderboardController {

    @FXML private VBox firstPlaceCard;
    @FXML private VBox secondPlaceCard;
    @FXML private VBox thirdPlaceCard;

    @FXML private Label firstPlaceName;
    @FXML private Label firstPlaceValue;

    @FXML private Label secondPlaceName;
    @FXML private Label secondPlaceValue;

    @FXML private Label thirdPlaceName;
    @FXML private Label thirdPlaceValue;

    @FXML private VBox leaderboardList;

    @FXML
    private void initialize() {
        setLeaderboard();
    }

    private void setLeaderboard() {
        ProblemApplication app = App.getApplication();
        if (app == null) {
            setEmptyState("Application not ready.");
            return;
        }

        List<User> topUsers = app.getLeaderboardTopPerformers(10);
        if (topUsers == null || topUsers.isEmpty()) {
            setEmptyState("No leaderboard data yet.");
            return;
        }

        setTopThree(topUsers);
        setRankList(topUsers);
    }

    private void setTopThree(List<User> users) {
        User first = users.size() > 0 ? users.get(0) : null;
        User second = users.size() > 1 ? users.get(1) : null;
        User third = users.size() > 2 ? users.get(2) : null;

        setRankingCard(firstPlaceCard, firstPlaceName, firstPlaceValue, first, "podium-gold");
        setRankingCard(secondPlaceCard, secondPlaceName, secondPlaceValue, second, "podium-silver");
        setRankingCard(thirdPlaceCard, thirdPlaceName, thirdPlaceValue, third, "podium-bronze");
    }

    private void setRankingCard(VBox card, Label nameLabel, Label valueLabel, User user, String styleClass) {
        if (card == null || nameLabel == null || valueLabel == null) {
            return;
        }

        card.getStyleClass().removeAll("podium-gold", "podium-silver", "podium-bronze");
        card.getStyleClass().add(styleClass);

        if (user == null) {
            nameLabel.setText("—");
            valueLabel.setText("0 days");
            return;
        }

        nameLabel.setText(getDisplayName(user));
        valueLabel.setText(formatDayLabel(user.getStreak()));
    }

    private void setRankList(List<User> users) {
        if (leaderboardList == null) {
            return;
        }

        leaderboardList.getChildren().clear();

        for (int i = 0; i < users.size(); i++) {
            leaderboardList.getChildren().add(makeRankRow(i + 1, users.get(i)));
        }
    }

    private HBox makeRankRow(int rank, User user) {
        HBox row = new HBox(10);
        row.getStyleClass().add("leaderboard-row");

        Label rankLabel = new Label(String.valueOf(rank));
        rankLabel.getStyleClass().add("leaderboard-rank");

        Label nameLabel = new Label(getDisplayName(user));
        nameLabel.getStyleClass().add("leaderboard-name");

        Label streakLabel = new Label(formatDayLabel(user.getStreak()));
        streakLabel.getStyleClass().add("leaderboard-streak");
        streakLabel.setMinWidth(60);

        Label recentProblemLabel = new Label(getLastCompletedText(user));
        recentProblemLabel.getStyleClass().add("leaderboard-last-problem");
        recentProblemLabel.setWrapText(false);

        VBox textBlock = new VBox(2);
        textBlock.getChildren().addAll(nameLabel, recentProblemLabel);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(rankLabel, textBlock, spacer, streakLabel);
        return row;
    }

    private String formatDayLabel(int streak) {
        return streak + (streak == 1 ? " day" : " days");
    }

    private String getLastCompletedText(User user) {
        if (user == null || user.getProgressTracker() == null) {
            return "No completed problems yet";
        }

        ArrayList<Question> completed = user.getProgressTracker().getCompletedQuestionsByDifficulty();
        if (completed == null || completed.isEmpty()) {
            return "No completed problems yet";
        }

        Question last = completed.get(completed.size() - 1);
        if (last == null || last.getTitle() == null || last.getTitle().isBlank()) {
            return "Last completed unavailable";
        }

        return "Last completed: " + last.getTitle();
    }

    private String getDisplayName(User user) {
        if (user == null) {
            return "Unknown";
        }

        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }

        String username = user.getUsername();
        if (username != null && !username.isBlank()) {
            return username;
        }

        return "Unknown";
    }

    private void setEmptyState(String message) {
        if (leaderboardList != null) {
            leaderboardList.getChildren().clear();
            Label empty = new Label(message);
            empty.getStyleClass().add("favorites-item-muted");
            leaderboardList.getChildren().add(empty);
        }

        if (firstPlaceName != null) firstPlaceName.setText("—");
        if (firstPlaceValue != null) firstPlaceValue.setText("0 days");
        if (secondPlaceName != null) secondPlaceName.setText("—");
        if (secondPlaceValue != null) secondPlaceValue.setText("0 days");
        if (thirdPlaceName != null) thirdPlaceName.setText("—");
        if (thirdPlaceValue != null) thirdPlaceValue.setText("0 days");
    }

    @FXML
    private void openProgress() throws IOException {
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
}