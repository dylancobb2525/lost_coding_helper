package com.controllers;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.enums.Topic;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class QuestionsController {

    /** Canonical company keys in filter order; JSON tags must use these names only. */
    private static final List<String> ALLOWED_COMPANIES = List.of(
            "MICROSOFT", "APPLE", "AWS", "META");

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

    /** Loads optional PNGs from {@code company_logos/}; otherwise draws small built-in marks. */
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
                        Image img = new Image(new ByteArrayInputStream(bytes), size, size, true, true);
                        if (!img.isError()) {
                            ImageView iv = new ImageView(img);
                            iv.setFitWidth(size);
                            iv.setFitHeight(size);
                            iv.setPreserveRatio(true);
                            iv.setSmooth(true);
                            iv.getStyleClass().add("company-mark-image");
                            return iv;
                        }
                    }
                }
            } catch (IOException ignored) {
                // fall through to built-in mark
            }
        }
        return builtInCompanyMark(companyKey, size);
    }

    private static Node builtInCompanyMark(String companyKey, double size) {
        return switch (companyKey) {
            case "MICROSOFT" -> microsoftTileMark(size);
            case "APPLE" -> appleBlobMark(size);
            case "AWS" -> awsPillMark(size);
            case "META" -> metaPillMark(size);
            default -> new Region();
        };
    }

    private static Node microsoftTileMark(double size) {
        double cell = Math.max(3.5, size / 3.8);
        double gap = Math.max(0.8, cell * 0.14);
        GridPane g = new GridPane();
        g.setHgap(gap);
        g.setVgap(gap);
        Color[] colors = {
                Color.web("#F65314"), Color.web("#7CBB00"),
                Color.web("#00A4EF"), Color.web("#FFBB00")
        };
        int i = 0;
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                Rectangle rect = new Rectangle(cell, cell);
                rect.setFill(colors[i++]);
                rect.setArcWidth(1.2);
                rect.setArcHeight(1.2);
                g.add(rect, c, r);
            }
        }
        StackPane wrap = new StackPane(g);
        wrap.setPrefSize(size, size);
        wrap.setMaxSize(size, size);
        wrap.getStyleClass().add("company-mark-builtin");
        return wrap;
    }

    /**
     * Single-path apple silhouette (24×24 style icon), scaled to fit; reads cleanly at chip and row sizes.
     */
    private static Node appleBlobMark(double size) {
        SVGPath apple = new SVGPath();
        apple.setContent(
                "M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82"
                        + "-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04"
                        + "-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z");
        apple.setFill(Color.web("#2d2d2d"));
        StackPane wrap = new StackPane(apple);
        wrap.setPrefSize(size, size);
        wrap.setMaxSize(size, size);
        wrap.getStyleClass().add("company-mark-builtin");
        Bounds b = apple.getBoundsInLocal();
        double span = Math.max(b.getWidth(), b.getHeight());
        if (span > 1e-3) {
            double sc = (size * 0.88) / span;
            double cx = b.getMinX() + b.getWidth() / 2.0;
            double cy = b.getMinY() + b.getHeight() / 2.0;
            apple.getTransforms().add(new Scale(sc, sc, cx, cy));
        }
        return wrap;
    }

    private static Node awsPillMark(double size) {
        Label t = new Label("AWS");
        t.setFont(Font.font(null, FontWeight.BOLD, Math.max(8, size * 0.36)));
        t.setTextFill(Color.WHITE);
        StackPane pane = new StackPane(t);
        double w = Math.max(size * 1.25, size);
        pane.setPrefSize(w, size);
        pane.setMaxSize(w, size);
        double rad = Math.max(4, size * 0.22);
        pane.setStyle("-fx-background-color: #FF9900; -fx-background-radius: " + rad + "px;");
        pane.getStyleClass().add("company-mark-builtin");
        return pane;
    }

    private static Node metaPillMark(double size) {
        Label t = new Label("Meta");
        t.setFont(Font.font(null, FontWeight.BOLD, Math.max(7, size * 0.3)));
        t.setTextFill(Color.WHITE);
        StackPane pane = new StackPane(t);
        double w = Math.max(size * 1.45, size);
        pane.setPrefSize(w, size);
        pane.setMaxSize(w, size);
        double rad = Math.max(4, size * 0.22);
        pane.setStyle("-fx-background-color: #0182fb; -fx-background-radius: " + rad + "px;");
        pane.getStyleClass().add("company-mark-builtin");
        return pane;
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
