package com.example.planify;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("loginWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 580);

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/planify/images/appIcon.png")));
        stage.getIcons().add(icon);

        stage.setResizable(false); // Запрещаем изменение размера окна
        stage.setTitle("Planify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}