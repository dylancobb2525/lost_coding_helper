package com.controllers;

import java.io.IOException;
import com.lost_coding_helper.App;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}
