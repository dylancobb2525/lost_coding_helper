package com.controllers;

import com.lost_coding_helper.App;
import com.model.LearningPlan;
import com.model.PlannerStep;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class HomeController {

    @FXML
    private VBox welcomePane;

    @FXML
    private VBox dashboardPane;

    @FXML
    private Label dashboardGreeting;

    @FXML
    private Label streakNumberLabel;

    @FXML
    private Label streakUnitLabel;

    @FXML
    private Label streakMotivationLabel;

    @FXML
    private ImageView streakImageView;

    @FXML
    private VBox streakCopyColumn;

    @FXML
    private Label statAllTimeLabel;

    @FXML
    private Label statTodayCompletedLabel;

    @FXML
    private Label randomProblemTitle;

    @FXML
    private Label randomProblemMeta;

    @FXML
    private Label studyPlanTitle;

    @FXML
    private Label studyPlanMeta;

    @FXML
    private Label randomSnippetLabel;

    @FXML
    private Label studySnippetLabel;

    @FXML
    private VBox favoritesList;

    /** Shown on the dashboard tiles; used when Open is pressed. */
    private Question dashboardRandomQuestion;
    private Question dashboardStudyPlanQuestion;

    @FXML
    private void initialize() {
        // So wrapped streak text stays inside the card (JavaFX needs a width to wrap against).
        bindStreakLabelsToCopyColumn();
        applyLoggedInState();
    }

    private void bindStreakLabelsToCopyColumn() {
        if (streakCopyColumn == null) {
            return;
        }
        Label[] labels = { streakNumberLabel, streakUnitLabel, streakMotivationLabel };
        for (Label label : labels) {
            if (label != null) {
                label.maxWidthProperty().bind(streakCopyColumn.widthProperty());
            }
        }
    }

    private void applyLoggedInState() {
        ProblemApplication app = App.getApplication();
        User user = app != null ? app.getCurrentUser() : null;
        boolean loggedIn = user != null;
        if (welcomePane != null) {
            welcomePane.setVisible(!loggedIn);
            welcomePane.setManaged(!loggedIn);
        }
        if (dashboardPane != null) {
            dashboardPane.setVisible(loggedIn);
            dashboardPane.setManaged(loggedIn);
        }
        if (loggedIn && dashboardGreeting != null) {
            String name = user.getDisplayName();
            if (name == null || name.isBlank()) {
                name = user.getUsername() != null ? user.getUsername() : "there";
            }
            dashboardGreeting.setText("Hi, " + name);
        }
        if (loggedIn) {
            hydrateDashboard(user, app);
        }
    }

    private void hydrateDashboard(User user, ProblemApplication app) {
        if (user != null) {
            int streak = user.getStreak();
            if (streakNumberLabel != null) {
                streakNumberLabel.setText(String.valueOf(streak));
            }
            if (streakUnitLabel != null) {
                streakUnitLabel.setText("DAY STREAK");
            }
            if (streakMotivationLabel != null) {
                streakMotivationLabel.setText(motivationForStreak(streak));
            }
            updateStreakImage(streak);
        }

        if (app == null) {
            return;
        }

        if (user != null) {
            int allTime = user.getProgressTracker() != null
                    ? user.getProgressTracker().getCurrentCount()
                    : app.getCompletedQuestion().size();
            if (statAllTimeLabel != null) {
                statAllTimeLabel.setText(String.valueOf(allTime));
            }
            int today = user.getProgressTracker() != null
                    ? user.getProgressTracker().getCompletionsToday()
                    : 0;
            if (statTodayCompletedLabel != null) {
                statTodayCompletedLabel.setText(String.valueOf(today));
            }
        }

        ArrayList<Question> all = app.getAllQuestions();
        dashboardRandomQuestion = pickRandom(all);
        if (randomProblemTitle != null) {
            randomProblemTitle.setText(dashboardRandomQuestion != null && dashboardRandomQuestion.getTitle() != null
                    ? dashboardRandomQuestion.getTitle() : "Random problem");
        }
        if (randomProblemMeta != null) {
            randomProblemMeta.setText(metaFor(dashboardRandomQuestion, "Easy", "Java"));
        }
        if (randomSnippetLabel != null) {
            randomSnippetLabel.setText(snippetFor(dashboardRandomQuestion, "Pick a problem and start coding."));
        }

        dashboardStudyPlanQuestion = pickFromStudyPlan(app);
        if (studyPlanTitle != null) {
            studyPlanTitle.setText(dashboardStudyPlanQuestion != null && dashboardStudyPlanQuestion.getTitle() != null
                    ? dashboardStudyPlanQuestion.getTitle() : "Study plan");
        }
        if (studyPlanMeta != null) {
            studyPlanMeta.setText(metaFor(dashboardStudyPlanQuestion, "Easy", "Java"));
        }
        if (studySnippetLabel != null) {
            studySnippetLabel.setText(snippetFor(dashboardStudyPlanQuestion,
                    "Today's plan skips what you already finished."));
        }

        if (favoritesList != null) {
            favoritesList.getChildren().clear();
            if (user != null) {
                List<Question> favorites = user.getFavoriteProblems();
                if (favorites != null) {
                    int shown = 0;
                    for (Question q : favorites) {
                        if (q == null || q.getTitle() == null) {
                            continue;
                        }
                        favoritesList.getChildren().add(favoriteRow(q));
                        shown++;
                        if (shown >= 3) {
                            break;
                        }
                    }
                }
            }
            if (favoritesList.getChildren().isEmpty()) {
                Label item = new Label("No favorites yet");
                item.getStyleClass().add("favorites-item-muted");
                favoritesList.getChildren().add(item);
            }
        }
    }

    private HBox favoriteRow(Question q) {
        HBox row = new HBox(10);
        row.getStyleClass().add("favorites-row");
        SVGPath star = new SVGPath();
        star.getStyleClass().addAll("favorites-star", "favorites-star-clickable");
        star.setContent("M12 2l2.9 6.6 7.1.6-5.4 4.6 1.6 7-6.2-3.7-6.2 3.7 1.6-7L2 9.2l7.1-.6L12 2z");
        star.setPickOnBounds(true);
        star.setCursor(Cursor.HAND);
        star.setOnMouseClicked(e -> confirmRemoveFavorite(q));
        String title = q.getTitle() != null ? q.getTitle() : "";
        Label label = new Label(title);
        label.getStyleClass().add("favorites-label");
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
            applyLoggedInState();
        }
    }

    private static Question pickRandom(List<Question> all) {
        if (all == null || all.isEmpty()) {
            return null;
        }
        int index = new Random().nextInt(all.size());
        return all.get(index);
    }

    private static String metaFor(Question q, String fallbackDifficulty, String fallbackLanguage) {
        String difficulty = q != null && q.getDifficulty() != null && !q.getDifficulty().isBlank()
                ? q.getDifficulty()
                : fallbackDifficulty;
        return difficulty + " · " + fallbackLanguage;
    }

    private static String snippetFor(Question q, String fallback) {
        if (q == null || q.getPrompt() == null || q.getPrompt().isBlank()) {
            return fallback;
        }
        String p = q.getPrompt().replaceAll("\\s+", " ").trim();
        if (p.length() <= 90) {
            return p;
        }
        return p.substring(0, 87) + "…";
    }

    private static final String STREAK_IMAGE_PREFIX = "/com/lost_coding_helper/streak_images/";

    // Which picture to show (0.png, 1.png, 5.png, 10.png, or 20.png in resources).
    private static String streakImageFileFor(int streak) {
        if (streak <= 0) {
            return "0.png";
        }
        if (streak < 5) {
            return "1.png";
        }
        if (streak < 10) {
            return "5.png";
        }
        if (streak < 20) {
            return "10.png";
        }
        return "20.png";
    }

    private void updateStreakImage(int streak) {
        if (streakImageView == null) {
            return;
        }
        String file = streakImageFileFor(streak);
        URL url = resolveStreakImage(file);
        if (url == null) {
            url = resolveStreakImage("0.png");
        }
        if (url != null) {
            streakImageView.setImage(new Image(url.toExternalForm(), true));
        }
    }

    private URL resolveStreakImage(String fileName) {
        URL classpathUrl = HomeController.class.getResource(STREAK_IMAGE_PREFIX + fileName);
        if (classpathUrl != null) {
            return classpathUrl;
        }

        Path fsPath = Path.of("src", "main", "resources", "com", "lost_coding_helper", "streak_images", fileName);
        if (!Files.exists(fsPath)) {
            return null;
        }

        try {
            return fsPath.toUri().toURL();
        } catch (IOException ignored) {
            return null;
        }
    }

    private static String motivationForStreak(int streak) {
        if (streak <= 0) {
            return "Start today, one solve will light the spark.";
        }
        if (streak <= 2) {
            return "You're gathering kindling — come back tomorrow.";
        }
        if (streak == 3) {
            return "Your campfire is growing.";
        }
        if (streak < 10) {
            return "Keep the flame alive — consistency is everything.";
        }
        return "Great job — you're on fire. Keep going!";
    }

    private static Question pickFromStudyPlan(ProblemApplication app) {
        try {
            LearningPlan plan = app.createStudyPlan("Java", 1);
            if (plan == null) return null;
            List<PlannerStep> steps = plan.getSteps();
            if (steps == null || steps.isEmpty()) return null;
            PlannerStep first = steps.get(0);
            if (first == null || first.getQuestionIds() == null || first.getQuestionIds().isEmpty()) return null;
            return app.getQuestionById(first.getQuestionIds().get(0));
        } catch (Exception ignored) {
            return null;
        }
    }

    @FXML
    private void goToLogin() throws IOException {
        App.setAuthInitialView(App.AuthInitialView.LOGIN);
        App.setRoot("login");
    }

    @FXML
    private void goToSignUp() throws IOException {
        App.setAuthInitialView(App.AuthInitialView.SIGN_UP);
        App.setRoot("login");
    }

    @FXML
    private void continueAsGuest() {
        if (App.getApplication() == null) {
            return;
        }
        App.getApplication().loginAsGuest();
        applyLoggedInState();
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

    @FXML
    private void openSettings() throws IOException {
        App.setRoot("profile");
    }

    @FXML
    private void openInfo() throws IOException {
        App.setRoot("info");
    }

    @FXML
    private void openRandomProblem() throws IOException {
        openQuestionDetail(dashboardRandomQuestion);
    }

    @FXML
    private void openStudyPlanProblem() throws IOException {
        openQuestionDetail(dashboardStudyPlanQuestion);
    }

    private static void openQuestionDetail(Question q) throws IOException {
        if (q == null || q.getId() == null) {
            return;
        }
        App.setSelectedQuestionId(q.getId());
        App.setSelectedQuestion(q);
        App.setRoot("question_detail");
    }
}
