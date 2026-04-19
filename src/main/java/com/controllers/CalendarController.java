package com.controllers;

import com.lost_coding_helper.App;
import javafx.fxml.FXML;

import java.io.IOException;

public class CalendarController {

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

