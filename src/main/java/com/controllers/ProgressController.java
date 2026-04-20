package com.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.User;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

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

        if (streakValueLabel != null) {
            streakValueLabel.setText(String.valueOf(user.getStreak()));
        }

        int solvedCount = user.getProgressTracker() != null
                ? user.getProgressTracker().getCurrentCount()
                : 0;
        if (solvedValueLabel != null) {
            solvedValueLabel.setText(String.valueOf(solvedCount));
        }

        int badgeCount = user.getAchievementIds() != null
                ? user.getAchievementIds().size()
                : 0;
        if (badgesValueLabel != null) {
            badgesValueLabel.setText(String.valueOf(badgeCount));
        }

        setBadges(user, badgeCount);
        setCompletedQuestions(user);
        setFavoriteQuestions(user);
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

    private void setBadges(User user, int badgeCount) {
        if (badgesPane == null) {
            return;
        }

        badgesPane.getChildren().clear();

        if (badgeCount <= 0) {
            badgesPane.getChildren().add(makeMutedLabel("No badges earned yet"));
            return;
        }

        for (int i = 0; i < badgeCount; i++) {
            badgesPane.getChildren().add(makeBadgeItem(i));
        }
    }

    private VBox makeBadgeItem(int index) {
        VBox badgeBox = new VBox(6);
        badgeBox.setAlignment(Pos.CENTER);
        badgeBox.getStyleClass().add("badge-item");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(42);
        imageView.setFitHeight(42);
        imageView.setPreserveRatio(true);

        String fileName = badgeFileName(index);
        URL url = ProgressController.class.getResource(BADGE_IMAGE_PREFIX + fileName);
        if (url != null) {
            imageView.setImage(new Image(url.toExternalForm(), true));
        }

        Label label = new Label(badgeLabel(index));
        label.getStyleClass().add("badge-label");

        badgeBox.getChildren().addAll(imageView, label);
        return badgeBox;
    }

    private String badgeFileName(int index) {
        switch (index % 3) {
            case 0:
                return "streak_badge.png";
            case 1:
                return "solved_badge.png";
            default:
                return "achievement_badge.png";
        }
    }

    private String badgeLabel(int index) {
        switch (index % 3) {
            case 0:
                return "Streak";
            case 1:
                return "Solved";
            default:
                return "Achievement";
        }
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

        addQuestionTitles(completedQuestionsList, completed, completed.size(), "No completed questions yet");
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

        addFavoriteQuestionRows(favorites);
    }

    private void addFavoriteQuestionRows(List<Question> favorites) {
        for (Question question : favorites) {
            if (question == null || question.getTitle() == null || question.getTitle().isBlank()) {
                continue;
            }
            favoriteQuestionsList.getChildren().add(makeFavoriteRow(question));
        }
    }

    private HBox makeFavoriteRow(Question q) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("progress-list-item");

        SVGPath star = new SVGPath();
        star.getStyleClass().addAll("favorites-star", "favorites-star-clickable");
        star.setContent("M12 2l2.9 6.6 7.1.6-5.4 4.6 1.6 7-6.2-3.7-6.2 3.7 1.6-7L2 9.2l7.1-.6L12 2z");
        star.setPickOnBounds(true);
        star.setCursor(Cursor.HAND);
        star.setOnMouseClicked(e -> confirmRemoveFavorite(q));

        Label label = new Label(q.getTitle());
        label.getStyleClass().add("progress-list-label");

        row.getChildren().addAll(star, label);
        return row;
    }

    private void confirmRemoveFavorite(Question q) {
        ProblemApplication app = App.getApplication();
        User user = app != null ? app.getCurrentUser() : null;
        if (app == null || user == null || q == null || !user.isFavoriteProblem(q)) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove favorite");
        alert.setHeaderText(null);
        String name = q.getTitle() != null && !q.getTitle().isBlank() ? q.getTitle() : "this problem";
        alert.setContentText("Remove \"" + name + "\" from your favorites?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            app.toggleFavoriteForCurrentUser(q);
            setProgress();
        }
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
        App.setRoot("questions");
    }

    @FXML
    private void navProfile() throws IOException {
        App.setRoot("profile");
    }
}