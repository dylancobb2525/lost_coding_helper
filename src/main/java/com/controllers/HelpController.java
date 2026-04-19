package com.controllers;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.enums.Topic;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class HelpController {
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

        Set<String> companies = new LinkedHashSet<>();
        for (Question question : allQuestions) {
            if (question == null || question.getCompanyTags() == null) {
                continue;
            }
            for (String company : question.getCompanyTags()) {
                if (company != null && !company.isBlank()) {
                    companies.add(company.trim().toUpperCase(Locale.ROOT));
                }
            }
        }

        ToggleButton all = createFilterChip("All", selectedCompany == null);
        all.setOnAction(e -> {
            selectedCompany = null;
            renderCompanyFilters();
            renderQuestions();
        });
        companyFiltersPane.getChildren().add(all);

        for (String company : companies) {
            ToggleButton chip = createFilterChip(formatCompany(company), company.equals(selectedCompany));
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
            if (company != null && selectedCompany.equals(company.trim().toUpperCase(Locale.ROOT))) {
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label difficulty = new Label(formatDifficulty(question.getDifficulty()));
        difficulty.getStyleClass().add("question-row-difficulty");

        row.getChildren().addAll(title, spacer, difficulty);
        return row;
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
        String normalized = company.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
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

