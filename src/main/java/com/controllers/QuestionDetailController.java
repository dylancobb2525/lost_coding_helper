package com.controllers;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.Solution;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private Question selectedQuestion;

    @FXML
    private void initialize() {
        if (languageCombo != null) {
            languageCombo.getItems().setAll("Java", "C++", "Python");
            languageCombo.getSelectionModel().selectFirst();
            languageCombo.setOnAction(e -> refreshOfficialSolutions());
        }
        hydrateQuestion();
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
        if (completionStatusLabel != null) {
            completionStatusLabel.setText("Official solutions are shown below for you to read and compare yourself.");
        }
        refreshOfficialSolutions();
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
        if (completionStatusLabel != null) {
            completionStatusLabel.setText("Marked complete.");
        }
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

        if (completionStatusLabel != null) {
            completionStatusLabel.setText("Your solution was saved (not checked against the official one).");
        }
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
