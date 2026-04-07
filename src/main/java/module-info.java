module com.lost_coding_helper {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires junit;

    opens com.lost_coding_helper to javafx.fxml;
    opens com.controllers to javafx.fxml;
    exports com.lost_coding_helper;
    exports com.model.enums;
    exports com.model;
    exports com.controllers;
}
