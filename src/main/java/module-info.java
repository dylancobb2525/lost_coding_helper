module com.lost_coding_helper {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;

    opens com.lost_coding_helper to javafx.fxml;
    exports com.lost_coding_helper;
}
