package com.controllers;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.Solution;
import com.model.User;
import com.model.enums.Topic;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestionsController {

    /** Canonical company keys in filter order; JSON tags must use these names only. */
    private static final List<String> ALLOWED_COMPANIES = List.of(
            "MICROSOFT", "APPLE", "AWS", "META");
    private static final List<String> SUPPORTED_LANGUAGES = List.of("Java", "C++", "Python");

    @FXML
    private TextField searchField;

    @FXML
    private FlowPane topicFiltersPane;

    @FXML
    private FlowPane companyFiltersPane;

    @FXML
    private Label allQuestionsLabel;

    @FXML
    private VBox questionListPane;

    private final List<Question> allQuestions = new ArrayList<>();
    private String selectedTopic;
    private String selectedCompany;

    @FXML
    private void initialize() {
        ProblemApplication app = App.getApplication();
        if (app != null && app.getAllQuestions() != null) {
            allQuestions.clear();
            allQuestions.addAll(app.getAllQuestions());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> renderQuestions());
        }

        renderTopicFilters();
        renderCompanyFilters();
        renderQuestions();
    }

    private void renderTopicFilters() {
        if (topicFiltersPane == null) {
            return;
        }
        topicFiltersPane.getChildren().clear();

        Set<String> topics = new LinkedHashSet<>();
        for (Question question : allQuestions) {
            if (question == null || question.getTopics() == null) {
                continue;
            }
            for (Topic topic : question.getTopics()) {
                if (topic != null) {
                    topics.add(topic.name());
                }
            }
        }

        ToggleButton all = createFilterChip("All", selectedTopic == null);
        all.setOnAction(e -> {
            selectedTopic = null;
            renderTopicFilters();
            renderQuestions();
        });
        topicFiltersPane.getChildren().add(all);

        for (String topic : topics) {
            ToggleButton chip = createFilterChip(formatTopic(topic), topic.equals(selectedTopic));
            chip.setOnAction(e -> {
                selectedTopic = topic.equals(selectedTopic) ? null : topic;
                renderTopicFilters();
                renderQuestions();
            });
            topicFiltersPane.getChildren().add(chip);
        }
    }

    private void renderCompanyFilters() {
        if (companyFiltersPane == null) {
            return;
        }
        companyFiltersPane.getChildren().clear();

        ToggleButton all = createFilterChip("All", selectedCompany == null);
        all.setOnAction(e -> {
            selectedCompany = null;
            renderCompanyFilters();
            renderQuestions();
        });
        companyFiltersPane.getChildren().add(all);

        for (String company : ALLOWED_COMPANIES) {
            ToggleButton chip = createCompanyFilterChip(company, company.equals(selectedCompany));
            chip.setOnAction(e -> {
                selectedCompany = company.equals(selectedCompany) ? null : company;
                renderCompanyFilters();
                renderQuestions();
            });
            companyFiltersPane.getChildren().add(chip);
        }
    }

    private ToggleButton createFilterChip(String text, boolean selected) {
        ToggleButton chip = new ToggleButton(text);
        chip.setSelected(selected);
        chip.getStyleClass().add("question-filter-chip");
        if (selected) {
            chip.getStyleClass().add("question-filter-chip-active");
        }
        chip.setFocusTraversable(false);
        return chip;
    }

    private ToggleButton createCompanyFilterChip(String companyKey, boolean selected) {
        ToggleButton chip = new ToggleButton();
        chip.setSelected(selected);
        chip.getStyleClass().addAll("question-filter-chip", "question-filter-chip-with-logo");
        if (selected) {
            chip.getStyleClass().add("question-filter-chip-active");
        }
        chip.setFocusTraversable(false);
        Node mark = companyMark(companyKey, 22);
        Label label = new Label(formatCompany(companyKey));
        label.getStyleClass().add("question-company-chip-label");
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(mark, label);
        chip.setGraphic(box);
        chip.setText("");
        return chip;
    }

    /** Loads PNG logos from {@code company_logos/}; falls back to plain text if missing. */
    private static Node companyMark(String companyKey, double size) {
        String file = switch (companyKey) {
            case "MICROSOFT" -> "microsoft.png";
            case "APPLE" -> "apple.png";
            case "AWS" -> "aws.png";
            case "META" -> "meta.png";
            default -> null;
        };
        if (file != null) {
            String path = "/com/lost_coding_helper/company_logos/" + file;
            try (InputStream stream = QuestionsController.class.getResourceAsStream(path)) {
                if (stream != null) {
                    byte[] bytes = stream.readAllBytes();
                    if (bytes.length > 0) {
                        // Load at natural resolution; scale in ImageView (clearer for wide logos like AWS).
                        Image img = new Image(new ByteArrayInputStream(bytes));
                        if (!img.isError()) {
                            ImageView iv = new ImageView(img);
                            configureCompanyLogoView(iv, companyKey, size);
                            iv.setSmooth(true);
                            iv.getStyleClass().add("company-mark-image");
                            return iv;
                        }
                    }
                }
            } catch (IOException ignored) {
                // fall through to text fallback
            }
        }
        return textFallbackMark(companyKey, size);
    }

    /**
     * Square fit for icon marks; AWS/Meta wordmarks are wide so they get extra width.
     */
    private static void configureCompanyLogoView(ImageView iv, String companyKey, double size) {
        iv.setPreserveRatio(true);
        if ("AWS".equals(companyKey) || "META".equals(companyKey)) {
            double h = Math.max(10, size * 0.92);
            double maxW = size <= 19 ? 54 : 72;
            iv.setFitHeight(h);
            iv.setFitWidth(maxW);
        } else {
            iv.setFitWidth(size);
            iv.setFitHeight(size);
        }
    }

    private static Node textFallbackMark(String companyKey, double size) {
        Label label = new Label(formatCompany(companyKey));
        label.getStyleClass().add("question-company-chip-label");
        StackPane wrap = new StackPane(label);
        double w = Math.max(size, "AWS".equals(companyKey) || "META".equals(companyKey) ? size * 1.6 : size * 1.2);
        wrap.setPrefSize(w, size);
        wrap.setMaxSize(w, size);
        wrap.getStyleClass().add("company-mark-builtin");
        return wrap;
    }

    private HBox buildCompanyMarksRow(Question question) {
        HBox logos = new HBox(6);
        logos.getStyleClass().add("question-row-companies");
        logos.setAlignment(Pos.CENTER_RIGHT);
        if (question.getCompanyTags() == null) {
            return logos;
        }
        Set<String> present = new HashSet<>();
        for (String raw : question.getCompanyTags()) {
            if (raw == null) {
                continue;
            }
            String u = raw.trim().toUpperCase(Locale.ROOT);
            if (ALLOWED_COMPANIES.contains(u)) {
                present.add(u);
            }
        }
        for (String key : ALLOWED_COMPANIES) {
            if (present.contains(key)) {
                StackPane wrap = new StackPane(companyMark(key, 18));
                wrap.getStyleClass().add("company-mark-row-wrap");
                logos.getChildren().add(wrap);
            }
        }
        return logos;
    }

    private void renderQuestions() {
        if (questionListPane == null) {
            return;
        }
        questionListPane.getChildren().clear();

        String query = searchField != null && searchField.getText() != null
                ? searchField.getText().trim().toLowerCase(Locale.ROOT)
                : "";

        List<Question> filtered = allQuestions.stream()
                .filter(q -> q != null && q.getTitle() != null)
                .filter(q -> matchesQuery(q, query))
                .filter(this::matchesTopic)
                .filter(this::matchesCompany)
                .collect(Collectors.toList());

        if (allQuestionsLabel != null) {
            allQuestionsLabel.setText("All questions (" + filtered.size() + ")");
        }

        if (filtered.isEmpty()) {
            Label empty = new Label("No questions match your current filters.");
            empty.getStyleClass().add("favorites-item-muted");
            questionListPane.getChildren().add(empty);
            return;
        }

        for (Question question : filtered) {
            questionListPane.getChildren().add(buildQuestionRow(question));
        }
    }

    private boolean matchesQuery(Question question, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String title = question.getTitle() != null ? question.getTitle().toLowerCase(Locale.ROOT) : "";
        String prompt = question.getPrompt() != null ? question.getPrompt().toLowerCase(Locale.ROOT) : "";
        return title.contains(query) || prompt.contains(query);
    }

    private boolean matchesTopic(Question question) {
        if (selectedTopic == null) {
            return true;
        }
        if (question.getTopics() == null) {
            return false;
        }
        for (Topic topic : question.getTopics()) {
            if (topic != null && selectedTopic.equals(topic.name())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCompany(Question question) {
        if (selectedCompany == null) {
            return true;
        }
        if (question.getCompanyTags() == null) {
            return false;
        }
        for (String company : question.getCompanyTags()) {
            if (company == null) {
                continue;
            }
            String u = company.trim().toUpperCase(Locale.ROOT);
            if (ALLOWED_COMPANIES.contains(u) && selectedCompany.equals(u)) {
                return true;
            }
        }
        return false;
    }

    private HBox buildQuestionRow(Question question) {
        HBox row = new HBox(12);
        row.getStyleClass().add("question-row");

        Label title = new Label(ellipsize(question.getTitle(), 38));
        title.getStyleClass().add("question-row-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox companyMarks = buildCompanyMarksRow(question);

        Label difficulty = new Label(formatDifficulty(question.getDifficulty()));
        difficulty.getStyleClass().add("question-row-difficulty");

        row.getChildren().addAll(title, companyMarks, difficulty);
        row.setOnMouseClicked(e -> openQuestion(question));
        return row;
    }

    private void openQuestion(Question question) {
        if (question == null) {
            return;
        }
        App.setSelectedQuestion(question);
        App.setSelectedQuestionId(question.getId());
        try {
            App.setRoot("question_detail");
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Open question failed");
            alert.setHeaderText(null);
            alert.setContentText("Could not open the selected question screen.");
            alert.showAndWait();
        }
    }

    private static String formatTopic(String topic) {
        if (topic == null) {
            return "";
        }
        if ("ALGORITHMS_DATASTRUCTURE".equals(topic)) {
            return "Algo";
        }
        if ("DATABASE".equals(topic)) {
            return "DB";
        }
        return topic.replace('_', ' ');
    }

    private static String formatCompany(String company) {
        if (company == null || company.isBlank()) {
            return "";
        }
        String u = company.trim().toUpperCase(Locale.ROOT);
        return switch (u) {
            case "AWS" -> "AWS";
            case "META" -> "Meta";
            case "MICROSOFT" -> "Microsoft";
            case "APPLE" -> "Apple";
            default -> {
                String n = company.trim().toLowerCase(Locale.ROOT);
                yield Character.toUpperCase(n.charAt(0)) + n.substring(1);
            }
        };
    }

    private static String formatDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "Easy";
        }
        String normalized = difficulty.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String ellipsize(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars - 1).trim() + "…";
    }

    @FXML
    private void openAddQuestionDialog() {
        ProblemApplication app = App.getApplication();
        if (app == null) {
            showInfo("Unavailable", "The application context is not ready right now.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add a question");
        DialogPane pane = dialog.getDialogPane();
        ButtonType saveButton = new ButtonType("Save question", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Question title");

        TextArea promptArea = new TextArea();
        promptArea.setPromptText("Describe the question prompt");
        promptArea.setWrapText(true);
        promptArea.setPrefRowCount(5);

        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().setAll(SUPPORTED_LANGUAGES);
        languageCombo.setPromptText("Select language");
        languageCombo.setMaxWidth(Double.MAX_VALUE);

        TextArea solutionArea = new TextArea();
        solutionArea.setPromptText("Add the solution code");
        solutionArea.setWrapText(true);
        solutionArea.setPrefRowCount(7);

        VBox form = new VBox(8,
                new Label("Title"),
                titleField,
                new Label("Prompt"),
                promptArea,
                new Label("Solution language"),
                languageCombo,
                new Label("Solution"),
                solutionArea
        );
        pane.setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != saveButton) {
            return;
        }

        String title = trim(titleField.getText());
        String prompt = trim(promptArea.getText());
        String language = languageCombo.getValue();
        String solutionCode = trim(solutionArea.getText());

        if (title.isBlank() || prompt.isBlank() || solutionCode.isBlank() || language == null || language.isBlank()) {
            showInfo(
                    "Missing details",
                    "Please provide a title, prompt, solution, and select a language before saving."
            );
            return;
        }

        User currentUser = app.getCurrentUser();
        UUID creatorId = currentUser != null ? currentUser.getUserId() : null;
        UUID questionId = UUID.randomUUID();

        Question question = new Question(
                questionId,
                title,
                prompt,
                "Easy",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                creatorId,
                java.time.LocalDateTime.now(),
                "PUBLISHED"
        );
        app.createQuestion(question);

        Solution solution = new Solution(
                UUID.randomUUID(),
                questionId,
                creatorId,
                solutionCode,
                language,
                "User submitted solution",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                0
        );
        app.addSolution(questionId, solution);
        boolean saved = app.saveAll();
        if (!saved) {
            showInfo("Save failed", "The question was created in memory, but saving to disk failed.");
            return;
        }

        allQuestions.clear();
        allQuestions.addAll(app.getAllQuestions());
        renderTopicFilters();
        renderCompanyFilters();
        renderQuestions();
        showInfo("Question added", "Your question and solution were added successfully.");
    }

    private static String trim(String text) {
        return text == null ? "" : text.trim();
    }

    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
