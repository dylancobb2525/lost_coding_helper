package com.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.lost_coding_helper.App;
import com.model.LearningPlan;
import com.model.PlannerStep;
import com.model.ProblemApplication;
import com.model.Question;
import com.model.enums.Topic;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CalendarController {

    @FXML private Label todayDateHeaderLabel;
    @FXML private Label plannerDateLabel;

    @FXML private ComboBox<String> studyLanguageCombo;
    @FXML private ComboBox<String> studyTopicCombo;
    @FXML private ComboBox<String> studyLevelCombo;

    @FXML private Label warmupLabel;
    @FXML private Label warmupSummaryLabel;
    @FXML private VBox warmupLinksVBox;

    @FXML private Label coreLabel;
    @FXML private Label coreSummaryLabel;
    @FXML private VBox coreLinksVBox;

    @FXML private Label stretchLabel;
    @FXML private Label stretchSummaryLabel;
    @FXML private VBox stretchLinksVBox;

    @FXML private Button day1;
    @FXML private Button day2;
    @FXML private Button day3;
    @FXML private Button day4;
    @FXML private Button day5;
    @FXML private Button day6;
    @FXML private Button day7;
    @FXML private Button day8;
    @FXML private Button day9;
    @FXML private Button day10;
    @FXML private Button day11;
    @FXML private Button day12;
    @FXML private Button day13;
    @FXML private Button day14;
    @FXML private Button day15;
    @FXML private Button day16;
    @FXML private Button day17;
    @FXML private Button day18;
    @FXML private Button day19;
    @FXML private Button day20;
    @FXML private Button day21;
    @FXML private Button day22;
    @FXML private Button day23;
    @FXML private Button day24;
    @FXML private Button day25;
    @FXML private Button day26;
    @FXML private Button day27;
    @FXML private Button day28;

    private final List<Button> dayButtons = new ArrayList<>();
    private YearMonth currentMonth;
    private int selectedDay;

    @FXML
    private void initialize() {
        currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();
        if (todayDateHeaderLabel != null) {
            DateTimeFormatter headerFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            todayDateHeaderLabel.setText("Today: " + headerFmt.format(today));
        }

        dayButtons.add(day1);
        dayButtons.add(day2);
        dayButtons.add(day3);
        dayButtons.add(day4);
        dayButtons.add(day5);
        dayButtons.add(day6);
        dayButtons.add(day7);
        dayButtons.add(day8);
        dayButtons.add(day9);
        dayButtons.add(day10);
        dayButtons.add(day11);
        dayButtons.add(day12);
        dayButtons.add(day13);
        dayButtons.add(day14);
        dayButtons.add(day15);
        dayButtons.add(day16);
        dayButtons.add(day17);
        dayButtons.add(day18);
        dayButtons.add(day19);
        dayButtons.add(day20);
        dayButtons.add(day21);
        dayButtons.add(day22);
        dayButtons.add(day23);
        dayButtons.add(day24);
        dayButtons.add(day25);
        dayButtons.add(day26);
        dayButtons.add(day27);
        dayButtons.add(day28);

        initStudyPreferenceCombos();

        int dom = today.getDayOfMonth();
        selectedDay = Math.min(dom, dayButtons.size());

        setupDayButtons();
        selectDay(selectedDay);
    }

    private void initStudyPreferenceCombos() {
        if (studyLanguageCombo != null) {
            studyLanguageCombo.getItems().setAll("Java", "C++", "Python");
            studyLanguageCombo.getSelectionModel().select("Java");
            studyLanguageCombo.setOnAction(e -> updatePlanner(selectedDay));
        }
        if (studyTopicCombo != null) {
            studyTopicCombo.getItems().setAll(
                    "Any topic",
                    "Algorithms & data structures",
                    "Database",
                    "Object-oriented programming"
            );
            studyTopicCombo.getSelectionModel().selectFirst();
            studyTopicCombo.setOnAction(e -> updatePlanner(selectedDay));
        }
        if (studyLevelCombo != null) {
            studyLevelCombo.getItems().setAll("Beginner", "Intermediate", "Advanced");
            studyLevelCombo.getSelectionModel().select(1);
            studyLevelCombo.setOnAction(e -> updatePlanner(selectedDay));
        }
    }

    private void setupDayButtons() {
        for (int i = 0; i < dayButtons.size(); i++) {
            Button button = dayButtons.get(i);
            int dayNumber = i + 1;

            button.setText(String.valueOf(dayNumber));
            button.getStyleClass().remove("calendar-day-selected");
            button.setOnAction(e -> selectDay(dayNumber));
        }
    }

    private void selectDay(int day) {
        selectedDay = day;

        for (Button button : dayButtons) {
            button.getStyleClass().remove("calendar-day-selected");
        }

        if (day >= 1 && day <= dayButtons.size()) {
            dayButtons.get(day - 1).getStyleClass().add("calendar-day-selected");
        }

        updatePlanner(day);
    }

    private void updatePlanner(int day) {
        LocalDate date = currentMonth.atDay(day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE \u2022 MMM d");

        if (plannerDateLabel != null) {
            plannerDateLabel.setText(formatter.format(date) + " \u2014 lineup for this day");
        }

        ProblemApplication app = App.getApplication();
        if (app == null) {
            clearPlannerUi("Open the app with data loaded to see your plan.");
            return;
        }

        String language = selectedLanguage();
        int level = selectedLevel();
        Topic focus = selectedTopic();

        LearningPlan plan = app.createDailyStudyPlan(date, language, level, focus);
        List<PlannerStep> steps = plan.getSteps();

        PlannerStep w = steps.size() > 0 ? steps.get(0) : null;
        PlannerStep c = steps.size() > 1 ? steps.get(1) : null;
        PlannerStep s = steps.size() > 2 ? steps.get(2) : null;

        applyStep("Warm-up", warmupLabel, warmupSummaryLabel, warmupLinksVBox, w, app);
        applyStep("Core", coreLabel, coreSummaryLabel, coreLinksVBox, c, app);
        applyStep("Stretch", stretchLabel, stretchSummaryLabel, stretchLinksVBox, s, app);
    }

    private void clearPlannerUi(String message) {
        if (warmupLabel != null) {
            warmupLabel.setText(message);
        }
        clearSummary(warmupSummaryLabel);
        clearLinks(warmupLinksVBox);
        if (coreLabel != null) {
            coreLabel.setText("");
        }
        clearSummary(coreSummaryLabel);
        clearLinks(coreLinksVBox);
        if (stretchLabel != null) {
            stretchLabel.setText("");
        }
        clearSummary(stretchSummaryLabel);
        clearLinks(stretchLinksVBox);
    }

    private static void clearSummary(Label label) {
        if (label != null) {
            label.setText("");
            label.setManaged(false);
            label.setVisible(false);
        }
    }

    private static void clearLinks(VBox box) {
        if (box != null) {
            box.getChildren().clear();
        }
    }

    private void applyStep(String phaseName,
                           Label head,
                           Label summary,
                           VBox linksBox,
                           PlannerStep step,
                           ProblemApplication app) {
        if (head == null || linksBox == null) {
            return;
        }
        if (step == null || step.getQuestionIds() == null || step.getQuestionIds().isEmpty()) {
            head.setText(phaseName + " \u2014 no problems matched");
            if (summary != null) {
                summary.setText("Try another language, focus, or level.");
                summary.setManaged(true);
                summary.setVisible(true);
            }
            linksBox.getChildren().clear();
            Label dash = new Label("\u2014");
            dash.getStyleClass().add("planner-muted");
            linksBox.getChildren().add(dash);
            return;
        }

        int n = step.getQuestionIds().size();
        head.setText(String.format("%s \u00b7 ~%d min \u00b7 %d problem%s",
                phaseName, step.getDurationMinutes(), n, n == 1 ? "" : "s"));

        // Titles appear only as hyperlinks below — avoid duplicating names as plain text.
        clearSummary(summary);

        populateQuestionLinks(linksBox, step, app);
    }

    private void populateQuestionLinks(VBox box, PlannerStep step, ProblemApplication app) {
        box.getChildren().clear();
        if (step.getQuestionIds() == null) {
            return;
        }
        for (UUID id : step.getQuestionIds()) {
            if (id == null) {
                continue;
            }
            Question q = app.getQuestionById(id);
            String title = q != null && q.getTitle() != null && !q.getTitle().isBlank()
                    ? q.getTitle().trim()
                    : "Open question";
            Hyperlink link = new Hyperlink(title);
            link.setWrapText(true);
            link.getStyleClass().add("planner-question-link");
            UUID target = id;
            link.setOnAction(e -> openQuestion(target));
            box.getChildren().add(link);
        }
    }

    private static void openQuestion(UUID questionId) {
        try {
            ProblemApplication app = App.getApplication();
            if (app == null || questionId == null) {
                return;
            }
            Question q = app.getQuestionById(questionId);
            App.setSelectedQuestionId(questionId);
            App.setSelectedQuestion(q);
            App.setRoot("question_detail");
        } catch (IOException ignored) {
            // stay on calendar
        }
    }

    private String selectedLanguage() {
        if (studyLanguageCombo == null || studyLanguageCombo.getValue() == null
                || studyLanguageCombo.getValue().isBlank()) {
            return "Java";
        }
        return studyLanguageCombo.getValue().trim();
    }

    private int selectedLevel() {
        if (studyLevelCombo == null) {
            return 2;
        }
        return switch (studyLevelCombo.getSelectionModel().getSelectedIndex()) {
            case 0 -> 1;
            case 2 -> 3;
            default -> 2;
        };
    }

    private Topic selectedTopic() {
        if (studyTopicCombo == null) {
            return null;
        }
        return switch (studyTopicCombo.getSelectionModel().getSelectedIndex()) {
            case 1 -> Topic.ALGORITHMS_DATASTRUCTURE;
            case 2 -> Topic.DATABASE;
            case 3 -> Topic.OOP;
            default -> null;
        };
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
