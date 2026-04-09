package com.controllers;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class LoginController {

    @FXML
    private VBox loginPane;

    @FXML
    private VBox signUpPane;

    @FXML
    private TextField loginUsernameField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private Label loginErrorLabel;

    @FXML
    private TextField signUpDisplayNameField;

    @FXML
    private TextField signUpUsernameField;

    @FXML
    private TextField signUpEmailField;

    @FXML
    private PasswordField signUpPasswordField;

    @FXML
    private PasswordField signUpConfirmPasswordField;

    @FXML
    private Label signUpErrorLabel;

    @FXML
    private void initialize() {
        App.AuthInitialView initial = App.consumeAuthInitialView();
        if (initial == App.AuthInitialView.SIGN_UP) {
            showSignUpPanel();
        } else {
            showLoginPanel();
        }
    }

    @FXML
    private void backToWelcome() throws IOException {
        App.setRoot("home");
    }

    @FXML
    private void backToLoginPanel() {
        clearSignUpForm();
        hideSignUpError();
        showLoginPanel();
    }

    @FXML
    private void showSignUpFromLogin() {
        hideLoginError();
        clearLoginForm();
        showSignUpPanel();
    }

    @FXML
    private void continueAsGuestFromLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Continue as guest");
        alert.setHeaderText(null);
        alert.setContentText("Guest mode is not implemented in the Java backend yet.");
        alert.showAndWait();
    }

    @FXML
    private void submitLogin() throws IOException {
        hideLoginError();
        ProblemApplication app = App.getApplication();
        if (app == null) {
            showLoginError("Application is not ready.");
            return;
        }
        String identity = loginUsernameField != null ? loginUsernameField.getText() : "";
        String password = loginPasswordField != null ? loginPasswordField.getText() : "";
        if (identity.isBlank() || password.isEmpty()) {
            showLoginError("Enter your username or email and password.");
            return;
        }
        User user = app.login(identity.trim(), password);
        if (user == null) {
            showLoginError("We could not sign you in. Check your username or email and password.");
            return;
        }
        App.setRoot("home");
    }

    @FXML
    private void submitSignUp() throws IOException {
        hideSignUpError();
        ProblemApplication app = App.getApplication();
        if (app == null) {
            showSignUpError("Application is not ready.");
            return;
        }
        String displayName = text(signUpDisplayNameField);
        String username = text(signUpUsernameField);
        String email = text(signUpEmailField);
        String password = signUpPasswordField != null ? signUpPasswordField.getText() : "";
        String confirm = signUpConfirmPasswordField != null ? signUpConfirmPasswordField.getText() : "";

        if (displayName.isBlank() || username.isBlank() || email.isBlank() || password.isEmpty()) {
            showSignUpError("Fill in every field.");
            return;
        }
        if (!password.equals(confirm)) {
            showSignUpError("Password and confirm password do not match.");
            return;
        }

        User created = app.createAccount(displayName.trim(), username.trim(), email.trim(), password);
        if (created == null) {
            showSignUpError("Could not create the account. Use a valid username (3–25 letters, numbers, _), a valid email, "
                    + "and a password with 8+ characters including upper, lower, and a digit. Username and email must be unique.");
            return;
        }
        app.saveAll();
        User loggedIn = app.login(username.trim(), password);
        if (loggedIn == null) {
            showSignUpError("Account was created but sign-in failed. Try Log in from the welcome screen.");
            showLoginPanel();
            return;
        }
        App.setRoot("home");
    }

    private static String text(TextField field) {
        return field != null && field.getText() != null ? field.getText().trim() : "";
    }

    private void showLoginPanel() {
        if (loginPane != null) {
            loginPane.setVisible(true);
            loginPane.setManaged(true);
        }
        if (signUpPane != null) {
            signUpPane.setVisible(false);
            signUpPane.setManaged(false);
        }
    }

    private void showSignUpPanel() {
        if (loginPane != null) {
            loginPane.setVisible(false);
            loginPane.setManaged(false);
        }
        if (signUpPane != null) {
            signUpPane.setVisible(true);
            signUpPane.setManaged(true);
        }
    }

    private void clearLoginForm() {
        if (loginUsernameField != null) {
            loginUsernameField.clear();
        }
        if (loginPasswordField != null) {
            loginPasswordField.clear();
        }
    }

    private void clearSignUpForm() {
        if (signUpDisplayNameField != null) {
            signUpDisplayNameField.clear();
        }
        if (signUpUsernameField != null) {
            signUpUsernameField.clear();
        }
        if (signUpEmailField != null) {
            signUpEmailField.clear();
        }
        if (signUpPasswordField != null) {
            signUpPasswordField.clear();
        }
        if (signUpConfirmPasswordField != null) {
            signUpConfirmPasswordField.clear();
        }
    }

    private void showLoginError(String message) {
        if (loginErrorLabel != null) {
            loginErrorLabel.setText(message);
            loginErrorLabel.setVisible(true);
            loginErrorLabel.setManaged(true);
        }
    }

    private void hideLoginError() {
        if (loginErrorLabel != null) {
            loginErrorLabel.setVisible(false);
            loginErrorLabel.setManaged(false);
        }
    }

    private void showSignUpError(String message) {
        if (signUpErrorLabel != null) {
            signUpErrorLabel.setText(message);
            signUpErrorLabel.setVisible(true);
            signUpErrorLabel.setManaged(true);
        }
    }

    private void hideSignUpError() {
        if (signUpErrorLabel != null) {
            signUpErrorLabel.setVisible(false);
            signUpErrorLabel.setManaged(false);
        }
    }
}
