package com.lost_coding_helper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("home"), 640, 480);
        URL cssUrl = App.class.getResource("styles.css");
        if (cssUrl == null) {
            throw new IllegalStateException("Missing CSS resource: styles.css");
        }
        scene.getStylesheets().add(cssUrl.toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        URL url = App.class.getResource(fxml + ".fxml");
        if (url == null) {
            throw new IllegalStateException("Missing FXML resource: " + fxml + ".fxml");
        }
        return new FXMLLoader(url).load();
    }

    public static void main(String[] args) {
        launch();
    }

}