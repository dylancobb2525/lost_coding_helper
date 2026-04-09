package com.controllers;

import com.lost_coding_helper.App;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class HomeController {

    @FXML
    private VBox welcomePane;

    @FXML
    private VBox dashboardPane;

    @FXML
    private Label dashboardGreeting;

    @FXML
    private void initialize() {
        applyLoggedInState();
    }

    private void applyLoggedInState() {
        User user = App.getApplication() != null ? App.getApplication().getCurrentUser() : null;
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Continue as guest");
        alert.setHeaderText(null);
        alert.setContentText("Guest mode is not implemented in the Java backend yet.");
        alert.showAndWait();
    }
}
