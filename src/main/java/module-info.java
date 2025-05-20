module com.example.planify {
    requires javafx.controlsEmpty; // Используйте имя из MANIFEST.MF
    requires javafx.fxmlEmpty;    // Используйте имя из MANIFEST.MF
    requires javafx.graphicsEmpty;     // Добавьте это, если используется
    requires javafx.baseEmpty;         // Добавьте это, если используется
    requires java.sql;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires jakarta.mail;
    requires java.desktop;

    opens com.example.planify to javafx.fxml; // Добавьте эту строку
    exports com.example.planify;
    exports Animations;
}