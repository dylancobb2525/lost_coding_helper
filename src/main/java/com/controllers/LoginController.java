package com.controllers;

import java.io.IOException;
import com.lost_coding_helper.App;
import javafx.fxml.FXML;

public class LoginController {

    @FXML
    private void switchToHome() throws IOException {
        App.setRoot("home");
    }
}