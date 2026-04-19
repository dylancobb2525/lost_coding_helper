package com.lost_coding_helper;

import com.model.ProblemApplication;
import com.model.Question;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * JavaFX App
 */
public class App extends Application {

    /** Which panel {@link com.controllers.LoginController} should show after {@code setRoot("login")}. */
    public enum AuthInitialView {
        LOGIN,
        SIGN_UP
    }

    private static Scene scene;
    private static ProblemApplication application;
    private static AuthInitialView authInitialView = AuthInitialView.LOGIN;
    private static UUID selectedQuestionId;
    private static Question selectedQuestion;

    @Override
    public void start(Stage stage) throws IOException {
        application = new ProblemApplication();
        application.init();

        scene = new Scene(loadFXML("home"), 390, 780);
        URL cssUrl = resolveResource("styles.css");
        if (cssUrl == null) {
            throw new IllegalStateException("Missing CSS resource: styles.css");
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setMinWidth(360);
        stage.setMinHeight(640);
        stage.setTitle("LOTS — Lord of the Strings");
        stage.setScene(scene);
        stage.show();
    }

    public static ProblemApplication getApplication() {
        return application;
    }

    public static void setAuthInitialView(AuthInitialView view) {
        authInitialView = view != null ? view : AuthInitialView.LOGIN;
    }

    /**
     * Consumed once when {@code login.fxml} loads so repeated navigation defaults to login.
     */
    public static AuthInitialView consumeAuthInitialView() {
        AuthInitialView v = authInitialView;
        authInitialView = AuthInitialView.LOGIN;
        return v;
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void setSelectedQuestionId(UUID questionId) {
        selectedQuestionId = questionId;
    }

    public static UUID getSelectedQuestionId() {
        return selectedQuestionId;
    }

    public static void setSelectedQuestion(Question question) {
        selectedQuestion = question;
    }

    public static Question getSelectedQuestion() {
        return selectedQuestion;
    }

    private static URL resolveResource(String resourceName) throws IOException {
        URL classpathUrl = App.class.getResource(resourceName);
        if (classpathUrl != null) {
            return classpathUrl;
        }

        Path fsPath = Path.of("src", "main", "resources", "com", "lost_coding_helper", resourceName);
        if (Files.exists(fsPath)) {
            return fsPath.toUri().toURL();
        }
        return null;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        URL url = resolveResource(fxml + ".fxml");
        if (url == null) {
            throw new IllegalStateException("Missing FXML resource: " + fxml + ".fxml");
        }
        return new FXMLLoader(url).load();
    }

    public static void main(String[] args) {
        launch();
    }

}
