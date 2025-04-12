module com.example.planify {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.planify to javafx.fxml;
    exports com.example.planify;
}