package com.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.lost_coding_helper.App;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CalendarController {

    @FXML private Label plannerDateLabel;
    @FXML private Label warmupLabel;
    @FXML private Label warmupSubLabel;
    @FXML private Label coreLabel;
    @FXML private Label coreSubLabel;
    @FXML private Label stretchLabel;
    @FXML private Label stretchSubLabel;

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
    private int selectedDay = 12; // matches your mockup better

    @FXML
    private void initialize() {
        currentMonth = YearMonth.now();

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

        setupDayButtons();
        selectDay(selectedDay);
    }

    private void setupDayButtons() {
        for (int i = 0; i < dayButtons.size(); i++) {
            Button button = dayButtons.get(i);
            int dayNumber = i + 1;

            button.setText("");
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
            plannerDateLabel.setText(formatter.format(date) + " \u2014 tap a day");
        }

        int mod = day % 4;

        switch (mod) {
            case 0 -> {
                warmupLabel.setText("Warm-up \u00b7 10 min");
                warmupSubLabel.setText("Two Sum, Valid Parentheses");

                coreLabel.setText("Core \u00b7 25 min");
                coreSubLabel.setText("Longest subarray, BST check");

                stretchLabel.setText("Stretch \u00b7 20 min");
                stretchSubLabel.setText("Hard graph (optional)");
            }
            case 1 -> {
                warmupLabel.setText("Warm-up \u00b7 10 min");
                warmupSubLabel.setText("Palindrome check, stack review");

                coreLabel.setText("Core \u00b7 25 min");
                coreSubLabel.setText("Binary search, merge intervals");

                stretchLabel.setText("Stretch \u00b7 20 min");
                stretchSubLabel.setText("Medium DP (optional)");
            }
            case 2 -> {
                warmupLabel.setText("Warm-up \u00b7 10 min");
                warmupSubLabel.setText("Hash map drill, anagrams");

                coreLabel.setText("Core \u00b7 25 min");
                coreSubLabel.setText("Trees, traversal practice");

                stretchLabel.setText("Stretch \u00b7 20 min");
                stretchSubLabel.setText("Greedy challenge (optional)");
            }
            default -> {
                warmupLabel.setText("Warm-up \u00b7 10 min");
                warmupSubLabel.setText("Array review, quick recursion");

                coreLabel.setText("Core \u00b7 25 min");
                coreSubLabel.setText("Linked list, sliding window");

                stretchLabel.setText("Stretch \u00b7 20 min");
                stretchSubLabel.setText("Backtracking (optional)");
            }
        }
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
