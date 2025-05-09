package com.example.planify;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * Main class for the Planify application.
 * This class initializes the JavaFX application and sets up the primary stage.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the login window FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("loginWindow.fxml"));

        // Set a fixed size for the window
        Scene scene = new Scene(fxmlLoader.load(), 960, 580);

        // Set the window icon
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/planify/images/appIcon.png")));
        stage.getIcons().add(icon);

        // Disable window resizing
        stage.setResizable(false);

        stage.setTitle("Planify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}