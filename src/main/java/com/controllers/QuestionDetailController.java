package com.controllers;

import com.lost_coding_helper.App;
import com.model.Comment;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.Solution;
import com.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestionDetailController {
    @FXML
    private Label questionTitleLabel;

    @FXML
    private Label questionMetaLabel;

    @FXML
    private Label questionTopicsLabel;

    @FXML
    private Label questionCompaniesLabel;

    @FXML
    private Label questionPromptLabel;

    @FXML
    private Label completionStatusLabel;

    @FXML
    private ComboBox<String> languageCombo;

    @FXML
    private TextArea solutionCodeArea;

    @FXML
    private VBox providedSolutionsContainer;

    @FXML
    private VBox officialSolutionsRevealPanel;

    @FXML
    private Button toggleOfficialSolutionButton;

    @FXML
    private Button favoriteButton;

    @FXML
    private ScrollPane commentsScrollPane;

    @FXML
    private VBox commentsListContainer;

    @FXML
    private TextArea commentInputArea;

    @FXML
    private Button postCommentButton;

    @FXML
    private VBox commentsDockRoot;

    @FXML
    private HBox commentsCollapsedStrip;

    @FXML
    private Label commentsCollapsedCount;

    @FXML
    private VBox commentsExpandedPanel;

    private Question selectedQuestion;

    /** Expanded comments panel uses this fraction of the window height (between nav and content). */
    private static final double COMMENTS_EXPANDED_SCENE_FRACTION = 0.35;

    private boolean commentsDockExpanded;

    private ChangeListener<Number> sceneHeightListener;

    private boolean officialSolutionRevealExpanded;

    @FXML
    private void initialize() {
        if (languageCombo != null) {
            languageCombo.getItems().setAll("Java", "C++", "Python");
            languageCombo.getSelectionModel().selectFirst();
            languageCombo.setOnAction(e -> refreshOfficialSolutions());
        }
        installCommentsDockSceneSizing();
        hydrateQuestion();
        applyCommentsDockVisualState(false);
    }

    private void installCommentsDockSceneSizing() {
        if (commentsDockRoot == null) {
            return;
        }
        commentsDockRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && sceneHeightListener != null) {
                oldScene.heightProperty().removeListener(sceneHeightListener);
                sceneHeightListener = null;
            }
            if (newScene != null) {
                sceneHeightListener = (o, ov, nv) -> {
                    if (commentsDockExpanded && nv.doubleValue() > 0) {
                        applyExpandedDockHeight(nv.doubleValue());
                    }
                };
                newScene.heightProperty().addListener(sceneHeightListener);
            }
        });
    }

    private void applyExpandedDockHeight(double sceneHeight) {
        if (commentsExpandedPanel == null || sceneHeight <= 0) {
            return;
        }
        double h = Math.clamp(sceneHeight * COMMENTS_EXPANDED_SCENE_FRACTION, 200, sceneHeight * 0.45);
        commentsExpandedPanel.setMaxHeight(h);
        commentsExpandedPanel.setPrefHeight(h);
    }

    private void applyCommentsDockVisualState(boolean expanded) {
        commentsDockExpanded = expanded;
        if (commentsCollapsedStrip != null) {
            commentsCollapsedStrip.setVisible(!expanded);
            commentsCollapsedStrip.setManaged(!expanded);
        }
        if (commentsExpandedPanel != null) {
            commentsExpandedPanel.setVisible(expanded);
            commentsExpandedPanel.setManaged(expanded);
        }
        if (expanded) {
            Scene scene = commentsDockRoot != null ? commentsDockRoot.getScene() : null;
            if (scene != null) {
                applyExpandedDockHeight(scene.getHeight());
            }
        }
    }

    private void applyOfficialSolutionRevealState(boolean expanded) {
        officialSolutionRevealExpanded = expanded;
        if (officialSolutionsRevealPanel != null) {
            officialSolutionsRevealPanel.setVisible(expanded);
            officialSolutionsRevealPanel.setManaged(expanded);
        }
        if (toggleOfficialSolutionButton != null) {
            toggleOfficialSolutionButton.setText(expanded ? "Hide ▲" : "Show ▼");
        }
    }

    @FXML
    private void toggleOfficialSolutionReveal() {
        applyOfficialSolutionRevealState(!officialSolutionRevealExpanded);
    }

    @FXML
    private void expandCommentsDock() {
        applyCommentsDockVisualState(true);
        Platform.runLater(this::scrollCommentsToBottom);
    }

    @FXML
    private void collapseCommentsDock() {
        applyCommentsDockVisualState(false);
    }

    private void clearCompletionHint() {
        if (completionStatusLabel != null) {
            completionStatusLabel.setText("");
            completionStatusLabel.setVisible(false);
            completionStatusLabel.setManaged(false);
        }
    }

    /** Shows short feedback under your solution (favorite, save, etc.). */
    private void applyCompletionFeedback(String message) {
        if (completionStatusLabel == null || message == null || message.isBlank()) {
            clearCompletionHint();
            return;
        }
        completionStatusLabel.setText(message);
        completionStatusLabel.setVisible(true);
        completionStatusLabel.setManaged(true);
    }

    private void hydrateQuestion() {
        ProblemApplication app = App.getApplication();
        UUID selectedId = App.getSelectedQuestionId();
        selectedQuestion = app != null ? app.getQuestionById(selectedId) : null;
        if (selectedQuestion == null) {
            selectedQuestion = App.getSelectedQuestion();
        }

        if (selectedQuestion == null) {
            if (questionTitleLabel != null) {
                questionTitleLabel.setText("Question not found");
            }
            if (questionPromptLabel != null) {
                questionPromptLabel.setText("Go back to the list and choose another question.");
            }
            refreshComments();
            refreshCommentPostingControls();
            clearCompletionHint();
            return;
        }

        if (questionTitleLabel != null) {
            questionTitleLabel.setText(selectedQuestion.getTitle() != null ? selectedQuestion.getTitle() : "Untitled");
        }
        if (questionMetaLabel != null) {
            questionMetaLabel.setText("Difficulty: " + formatDifficulty(selectedQuestion.getDifficulty()));
        }
        if (questionTopicsLabel != null) {
            String topics = selectedQuestion.getTopics() == null || selectedQuestion.getTopics().isEmpty()
                    ? "None"
                    : selectedQuestion.getTopics().stream()
                    .map(t -> t != null ? t.name().replace('_', ' ') : "")
                    .collect(Collectors.joining(", "));
            questionTopicsLabel.setText("Topics: " + topics);
        }
        if (questionCompaniesLabel != null) {
            String companies = selectedQuestion.getCompanyTags() == null || selectedQuestion.getCompanyTags().isEmpty()
                    ? "None"
                    : String.join(", ", selectedQuestion.getCompanyTags());
            questionCompaniesLabel.setText("Companies: " + companies);
        }
        if (questionPromptLabel != null) {
            questionPromptLabel.setText(selectedQuestion.getPrompt() != null
                    ? selectedQuestion.getPrompt().trim()
                    : "No prompt available.");
        }
        clearCompletionHint();
        applyOfficialSolutionRevealState(false);
        refreshFavoriteButton();
        refreshOfficialSolutions();
        refreshComments();
        refreshCommentPostingControls();
    }

    private void refreshComments() {
        try {
            if (commentsListContainer == null) {
                return;
            }
            commentsListContainer.getChildren().clear();
            if (selectedQuestion == null) {
                return;
            }
            ArrayList<Comment> raw = selectedQuestion.getComments();
            if (raw == null || raw.isEmpty()) {
                Label empty = new Label("No comments yet.");
                empty.getStyleClass().add("hint-muted");
                empty.setWrapText(true);
                commentsListContainer.getChildren().add(empty);
                scrollCommentsToBottom();
                return;
            }
            ArrayList<Comment> list = new ArrayList<>();
            for (Comment c : raw) {
                if (c != null && c.getBody() != null && !c.getBody().isBlank()) {
                    list.add(c);
                }
            }
            list.sort(Comparator.comparing(c -> c.getCreatedOn() != null ? c.getCreatedOn() : LocalDateTime.MIN));
            if (list.isEmpty()) {
                Label empty = new Label("No comments yet.");
                empty.getStyleClass().add("hint-muted");
                empty.setWrapText(true);
                commentsListContainer.getChildren().add(empty);
                scrollCommentsToBottom();
                return;
            }
            DateTimeFormatter tf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
            for (Comment c : list) {
                VBox row = new VBox(4);
                row.getStyleClass().add("comment-card");
                String author = c.getAuthorDisplayName();
                if (author == null || author.isBlank()) {
                    author = "User";
                }
                String timePart = c.getCreatedOn() != null ? tf.format(c.getCreatedOn()) : "";
                Label meta = new Label(timePart.isEmpty() ? author : author + " · " + timePart);
                meta.getStyleClass().add("card-meta");
                Label body = new Label(c.getBody());
                body.setWrapText(true);
                body.getStyleClass().add("favorites-item-muted");
                row.getChildren().addAll(meta, body);
                commentsListContainer.getChildren().add(row);
            }
            scrollCommentsToBottom();
        } finally {
            refreshCollapsedCommentSummary();
        }
    }

    private void refreshCollapsedCommentSummary() {
        if (commentsCollapsedCount == null) {
            return;
        }
        if (selectedQuestion == null) {
            commentsCollapsedCount.setText("");
            return;
        }
        int n = countRenderableComments(selectedQuestion);
        if (n == 0) {
            commentsCollapsedCount.setText("· none yet");
        } else if (n == 1) {
            commentsCollapsedCount.setText("· 1 comment");
        } else {
            commentsCollapsedCount.setText("· " + n + " comments");
        }
    }

    private static int countRenderableComments(Question q) {
        if (q.getComments() == null) {
            return 0;
        }
        int n = 0;
        for (Comment c : q.getComments()) {
            if (c != null && c.getBody() != null && !c.getBody().isBlank()) {
                n++;
            }
        }
        return n;
    }

    private void scrollCommentsToBottom() {
        Platform.runLater(() -> {
            if (commentsScrollPane != null) {
                commentsScrollPane.setVvalue(1);
            }
        });
    }

    private void refreshCommentPostingControls() {
        if (commentInputArea == null || postCommentButton == null) {
            return;
        }
        ProblemApplication app = App.getApplication();
        User user = app != null ? app.getCurrentUser() : null;
        boolean canPost = user != null && user.canSubmitSolutions();
        commentInputArea.setDisable(!canPost);
        postCommentButton.setDisable(!canPost);
        if (!canPost && user == null) {
            commentInputArea.setPromptText("Sign in to post a comment.");
        } else if (!canPost) {
            commentInputArea.setPromptText("Create an account to post comments.");
        } else {
            commentInputArea.setPromptText("Write a comment…");
        }
    }

    @FXML
    private void postQuestionComment() {
        ProblemApplication app = App.getApplication();
        if (app == null || selectedQuestion == null || selectedQuestion.getId() == null) {
            return;
        }
        User user = app.getCurrentUser();
        if (user == null) {
            showInfo("Sign in required", "You must be signed in to post a comment.");
            return;
        }
        if (!user.canSubmitSolutions()) {
            showInfo("Guest limitation", "Create an account to post comments on questions.");
            return;
        }
        String text = commentInputArea != null && commentInputArea.getText() != null
                ? commentInputArea.getText().trim()
                : "";
        if (text.isEmpty()) {
            showInfo("Empty comment", "Write something before posting.");
            return;
        }
        boolean saved = app.addQuestionComment(selectedQuestion.getId(), text);
        if (!saved) {
            showInfo("Could not save", "Your comment could not be saved. Try again.");
            return;
        }
        if (commentInputArea != null) {
            commentInputArea.clear();
        }
        selectedQuestion = app.getQuestionById(selectedQuestion.getId());
        refreshComments();
        applyCompletionFeedback("Comment posted.");
    }

    private void refreshFavoriteButton() {
        if (favoriteButton == null) {
            return;
        }
        ProblemApplication app = App.getApplication();
        User user = app != null ? app.getCurrentUser() : null;
        if (user == null || selectedQuestion == null) {
            favoriteButton.setDisable(true);
            favoriteButton.setText("Favorites");
            return;
        }
        if (!user.canFavoriteProblems()) {
            favoriteButton.setDisable(true);
            favoriteButton.setText("Favorites (account only)");
            return;
        }
        favoriteButton.setDisable(false);
        favoriteButton.setText(user.isFavoriteProblem(selectedQuestion) ? "Remove from favorites" : "Add to favorites");
    }

    /**
     * Bundled solutions in JSON use {@code authorId: null}. User-written saves use their user id and are not shown here.
     */
    private void refreshOfficialSolutions() {
        if (providedSolutionsContainer == null || selectedQuestion == null) {
            return;
        }
        providedSolutionsContainer.getChildren().clear();

        String selectedLang = languageCombo != null && languageCombo.getValue() != null
                ? canonicalLang(languageCombo.getValue())
                : "Java";

        List<Solution> official = new ArrayList<>();
        if (selectedQuestion.getSolutions() != null) {
            for (Solution s : selectedQuestion.getSolutions()) {
                if (s == null || s.getAuthorId() != null) {
                    continue;
                }
                if (s.getLanguage() == null || s.getLanguage().isBlank() || sameLanguage(s.getLanguage(), selectedLang)) {
                    official.add(s);
                }
            }
        }

        if (official.isEmpty()) {
            Label empty = new Label("No official solution is included for " + selectedLang + " on this question in the data file.");
            empty.getStyleClass().add("hint-muted");
            empty.setWrapText(true);
            providedSolutionsContainer.getChildren().add(empty);
            return;
        }

        for (Solution s : official) {
            Label header = new Label("Official — " + canonicalLang(s.getLanguage()));
            header.getStyleClass().add("section-title");

            TextArea code = new TextArea(s.getCode() != null ? s.getCode() : "");
            code.setEditable(false);
            code.setWrapText(true);
            code.getStyleClass().add("question-editor");
            code.setPrefRowCount(Math.min(14, Math.max(4, countLines(s.getCode()))));

            providedSolutionsContainer.getChildren().add(header);
            providedSolutionsContainer.getChildren().add(code);

            if (s.getExplanation() != null && !s.getExplanation().isBlank()) {
                Label exLabel = new Label("Notes");
                exLabel.getStyleClass().add("field-label");
                TextArea ex = new TextArea(s.getExplanation().trim());
                ex.setEditable(false);
                ex.setWrapText(true);
                ex.getStyleClass().add("question-editor");
                ex.setPrefRowCount(Math.min(8, Math.max(2, countLines(s.getExplanation()))));
                providedSolutionsContainer.getChildren().add(exLabel);
                providedSolutionsContainer.getChildren().add(ex);
            }
        }
    }

    private static int countLines(String s) {
        if (s == null || s.isEmpty()) {
            return 1;
        }
        int n = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                n++;
            }
        }
        return n;
    }

    private static String canonicalLang(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Java";
        }
        String t = raw.trim();
        if (t.equalsIgnoreCase("c++") || t.equalsIgnoreCase("cpp")) {
            return "C++";
        }
        if (t.equalsIgnoreCase("python")) {
            return "Python";
        }
        if (t.equalsIgnoreCase("java")) {
            return "Java";
        }
        return t;
    }

    private static boolean sameLanguage(String a, String b) {
        return canonicalLang(a).equalsIgnoreCase(canonicalLang(b));
    }

    private static String formatDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "Easy";
        }
        String normalized = difficulty.trim().toLowerCase();
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    @FXML
    private void markComplete() {
        ProblemApplication app = App.getApplication();
        User user = app != null ? app.getCurrentUser() : null;
        if (app == null || selectedQuestion == null) {
            return;
        }
        if (user == null) {
            showInfo("Sign in required", "You must be signed in to track completions.");
            return;
        }
        if (!user.canTrackProgress()) {
            showInfo("Guest limitation", "Guest users can view questions, but completion tracking requires an account.");
            return;
        }
        if (selectedQuestion.getId() == null) {
            showInfo("Unavailable", "This question is missing an ID, so completion cannot be saved yet.");
            return;
        }
        app.recordAttempt(selectedQuestion.getId(), 0);
        app.markCompleted(selectedQuestion.getId(), 0);
        applyCompletionFeedback("Marked complete.");
    }

    @FXML
    private void toggleFavorite() {
        ProblemApplication app = App.getApplication();
        User user = app != null ? app.getCurrentUser() : null;
        if (app == null || selectedQuestion == null || selectedQuestion.getId() == null) {
            return;
        }
        if (user == null) {
            showInfo("Sign in required", "Sign in to favorite questions.");
            return;
        }
        if (!user.canFavoriteProblems()) {
            showInfo("Guest limitation", "Create an account to save favorite problems.");
            return;
        }
        boolean favorited = app.toggleFavoriteForCurrentUser(selectedQuestion);
        refreshFavoriteButton();
        applyCompletionFeedback(favorited
                ? "Saved to your favorites (home and progress)."
                : "Removed from favorites.");
    }

    /**
     * Saves the user's work only. Does not compare or grade against official solutions.
     */
    @FXML
    private void saveMySolution() {
        ProblemApplication app = App.getApplication();
        User user = app != null ? app.getCurrentUser() : null;
        if (app == null || selectedQuestion == null || selectedQuestion.getId() == null) {
            return;
        }
        if (user == null) {
            showInfo("Sign in required", "You must be signed in to save your solution.");
            return;
        }
        if (!user.canSubmitSolutions()) {
            showInfo("Guest limitation", "Guest users can read official solutions; saving your own requires an account.");
            return;
        }

        String language = languageCombo != null && languageCombo.getValue() != null
                ? canonicalLang(languageCombo.getValue())
                : "Java";
        String code = solutionCodeArea != null && solutionCodeArea.getText() != null
                ? solutionCodeArea.getText().trim()
                : "";
        if (code.isBlank()) {
            showInfo("Missing code", "Write something in your solution area before saving.");
            return;
        }

        Solution solution = new Solution(
                UUID.randomUUID(),
                selectedQuestion.getId(),
                user.getUserId(),
                code,
                language,
                "",
                LocalDateTime.now(),
                LocalDateTime.now(),
                0
        );
        app.addSolution(selectedQuestion.getId(), solution);
        app.recordAttempt(selectedQuestion.getId(), 0);

        applyCompletionFeedback("Your solution was saved (not checked against the official one).");
    }

    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void backToQuestions() throws IOException {
        App.setRoot("questions");
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
