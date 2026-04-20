package com.controllers;

import com.lost_coding_helper.App;
import com.model.ProblemApplication;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class ProfileController {

    @FXML
    private Label displayNameValueLabel;

    @FXML
    private Label usernameValueLabel;

    @FXML
    private Label emailValueLabel;

    @FXML
    private StackPane profileAvatarShell;

    @FXML
    private SVGPath profilePlaceholderSvg;

    @FXML
    private ImageView profileImageView;

    @FXML
    private void initialize() {
        applyCircularProfileClip();
        if (profilePlaceholderSvg != null) {
            profilePlaceholderSvg.setScaleX(3.0);
            profilePlaceholderSvg.setScaleY(3.0);
        }
        hydrateUserInfo();
        loadProfileImageFromUser(getCurrentUser());
    }

    private void applyCircularProfileClip() {
        if (profileAvatarShell != null) {
            Circle clip = new Circle();
            clip.centerXProperty().bind(profileAvatarShell.widthProperty().divide(2));
            clip.centerYProperty().bind(profileAvatarShell.heightProperty().divide(2));
            clip.radiusProperty().bind(profileAvatarShell.widthProperty().divide(2));
            profileAvatarShell.setClip(clip);
        } else if (profileImageView != null) {
            Circle clip = new Circle();
            clip.centerXProperty().bind(profileImageView.fitWidthProperty().divide(2));
            clip.centerYProperty().bind(profileImageView.fitHeightProperty().divide(2));
            clip.radiusProperty().bind(profileImageView.fitWidthProperty().divide(2));
            profileImageView.setClip(clip);
        }
    }

    private void hydrateUserInfo() {
        User user = getCurrentUser();
        if (user == null) {
            displayNameValueLabel.setText("Guest");
            usernameValueLabel.setText("guest");
            emailValueLabel.setText("Not signed in");
            return;
        }
        displayNameValueLabel.setText(safeText(user.getDisplayName(), "Unknown"));
        usernameValueLabel.setText(safeText(user.getUsername(), "unknown"));
        emailValueLabel.setText(safeText(user.getEmail(), "no-email@lots.app"));
    }

    private static String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private User getCurrentUser() {
        ProblemApplication app = App.getApplication();
        return app == null ? null : app.getCurrentUser();
    }

    private void saveUsersSilently() {
        ProblemApplication app = App.getApplication();
        if (app == null) {
            return;
        }
        app.saveAll();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        themeDialog(alert, "profile-dialog");
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        themeDialog(alert, "profile-dialog profile-dialog-error");
        alert.showAndWait();
    }

    private void themeDialog(Dialog<?> dialog, String styleClasses) {
        if (dialog == null) {
            return;
        }
        DialogPane pane = dialog.getDialogPane();
        if (pane == null) {
            return;
        }
        URL cssUrl = ProfileController.class.getResource("/com/lost_coding_helper/styles.css");
        if (cssUrl != null && !pane.getStylesheets().contains(cssUrl.toExternalForm())) {
            pane.getStylesheets().add(cssUrl.toExternalForm());
        }
        if (styleClasses != null && !styleClasses.isBlank()) {
            for (String styleClass : styleClasses.split("\\s+")) {
                if (!styleClass.isBlank() && !pane.getStyleClass().contains(styleClass)) {
                    pane.getStyleClass().add(styleClass);
                }
            }
        }
    }

    @FXML
    private void changeDisplayName() {
        User user = getCurrentUser();
        if (user == null) {
            showInfo("Profile", "Sign in first to edit your profile.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(user.getDisplayName());
        dialog.setTitle("Change Display Name");
        dialog.setHeaderText(null);
        dialog.setContentText("New display name:");
        themeDialog(dialog, "profile-dialog");
        dialog.showAndWait().ifPresent(input -> {
            String before = user.getDisplayName();
            user.setDisplayName(input);
            if (!safeText(before, "").equals(user.getDisplayName())) {
                displayNameValueLabel.setText(user.getDisplayName());
                saveUsersSilently();
            } else {
                showError("Invalid Name", "Display name must be 3-25 characters.");
            }
        });
    }

    @FXML
    private void changeUsername() {
        User user = getCurrentUser();
        if (user == null) {
            showInfo("Profile", "Sign in first to edit your profile.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(user.getUsername());
        dialog.setTitle("Change Username");
        dialog.setHeaderText(null);
        dialog.setContentText("New username:");
        themeDialog(dialog, "profile-dialog");
        dialog.showAndWait().ifPresent(input -> {
            if (!user.setUsername(input)) {
                showError("Invalid Username", "Username must be 3-25 characters and use letters, numbers, or underscores.");
                return;
            }
            usernameValueLabel.setText(user.getUsername());
            saveUsersSilently();
        });
    }

    @FXML
    private void changeEmail() {
        User user = getCurrentUser();
        if (user == null) {
            showInfo("Profile", "Sign in first to edit your profile.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(user.getEmail());
        dialog.setTitle("Change Email");
        dialog.setHeaderText(null);
        dialog.setContentText("New email:");
        themeDialog(dialog, "profile-dialog");
        dialog.showAndWait().ifPresent(input -> {
            if (!user.setEmail(input)) {
                showError("Invalid Email", "Please enter a valid email address.");
                return;
            }
            emailValueLabel.setText(user.getEmail());
            saveUsersSilently();
        });
    }

    @FXML
    private void changePassword() {
        User user = getCurrentUser();
        if (user == null) {
            showInfo("Profile", "Sign in first to edit your profile.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        themeDialog(dialog, "profile-dialog");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("profile-dialog-grid");

        PasswordField oldPassword = new PasswordField();
        PasswordField newPassword = new PasswordField();
        oldPassword.setPromptText("Current password");
        newPassword.setPromptText("New password");
        oldPassword.getStyleClass().add("profile-dialog-field");
        newPassword.getStyleClass().add("profile-dialog-field");
        grid.add(new Label("Current:"), 0, 0);
        grid.add(oldPassword, 1, 0);
        grid.add(new Label("New:"), 0, 1);
        grid.add(newPassword, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String beforeHash = user.getHashedPassword();
        user.changePassword(oldPassword.getText(), newPassword.getText());
        if (!safeText(beforeHash, "").equals(safeText(user.getHashedPassword(), ""))) {
            saveUsersSilently();
            showInfo("Password Updated", "Your password was successfully changed.");
            return;
        }
        showError("Password Not Changed", "Check your current password and ensure the new one has uppercase, lowercase, number, and at least 8 chars.");
    }

    @FXML
    private void changeProfilePicture() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Picture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File selected = chooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selected == null) {
            return;
        }
        User user = getCurrentUser();
        if (user != null) {
            user.setProfilePhotoUri(selected.toURI().toString());
            saveUsersSilently();
        }
        loadProfileImageFromUser(user);
    }

    private void loadProfileImageFromUser(User user) {
        if (profileImageView != null) {
            profileImageView.setImage(null);
        }
        if (profilePlaceholderSvg != null) {
            profilePlaceholderSvg.setVisible(true);
        }
        if (user == null || user.getProfilePhotoUri() == null || user.getProfilePhotoUri().isBlank()) {
            return;
        }
        try {
            Image img = new Image(user.getProfilePhotoUri(), true);
            profileImageView.setImage(img);
            if (profilePlaceholderSvg != null) {
                profilePlaceholderSvg.setVisible(false);
            }
        } catch (Exception ignored) {
        }
    }

    @FXML
    private void logOut() throws IOException {
        ProblemApplication app = App.getApplication();
        if (app != null) {
            app.logOut();
        }
        App.setAuthInitialView(App.AuthInitialView.LOGIN);
        App.setRoot("login");
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

