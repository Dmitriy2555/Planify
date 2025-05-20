package com.example.planify;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AvatarHandler {

    private final Circle circleBackAvatar;
    private final User user;
    private final DatabaseHandler dbHandler;
    private final boolean isSettingsPage;

    public AvatarHandler(Circle circleBackAvatar, User user, boolean isSettingsPage) {
        this.circleBackAvatar = circleBackAvatar;
        this.user = user;
        this.dbHandler = new DatabaseHandler();
        this.isSettingsPage = isSettingsPage;
        setupEventHandlers();
        loadUserAvatar();
    }

    private void setupEventHandlers() {
        if (!isSettingsPage) return; // 💡 Только если страница "Settings"

        circleBackAvatar.setOnMouseClicked(event -> handleAvatarClick());
        circleBackAvatar.setOnMouseEntered(event -> handleAvatarHoverEnter());
        circleBackAvatar.setOnMouseExited(event -> handleAvatarHoverExit());
    }

    private void handleAvatarClick() {
        // Создаем диалоговое окно для выбора действия
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Profile Picture");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to change or delete your avatar?");
        alert.getDialogPane().setGraphic(null);

        // Кнопки для изменения и удаления
        ButtonType changeButton = new ButtonType("Change", ButtonType.OK.getButtonData());
        ButtonType removeButton = new ButtonType("Remove", ButtonType.OK.getButtonData());
        ButtonType cancelButton = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        alert.getButtonTypes().setAll(changeButton, removeButton, cancelButton);

        //Устанавливаем иконку для диалога
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconQuestion.png"));
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        // Показываем диалог и обрабатываем выбор
        alert.showAndWait().ifPresent(response -> {
            if (response == changeButton) {
                // Логика изменения аватара
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Profile Picture");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );

                File selectedFile = fileChooser.showOpenDialog(circleBackAvatar.getScene().getWindow());

                if (selectedFile != null) {
                    String fileName = selectedFile.getName().toLowerCase();
                    if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                        try {
                            String extension = fileName.substring(fileName.lastIndexOf('.'));
                            String newFileName = "user_" + user.getId() + extension;

                            Path userImagesDir = Paths.get(System.getProperty("user.dir"), "user_images");
                            if (!Files.exists(userImagesDir)) {
                                Files.createDirectories(userImagesDir);
                            }

                            Path destination = userImagesDir.resolve(newFileName);
                            Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                            // Сохраняем путь к аватару в базе данных
                            dbHandler.updateUserAvatar(user.getId(), destination.toString());

                            // Обновляем отображение аватара
                            setAvatarImage(destination.toUri().toString());

                            showSuccessAlert("Profile picture updated successfully!");
                        } catch (IOException e) {
                            e.printStackTrace();
                            showAlertOneButton("Failed to save the image. Please try again.");
                        }
                    } else {
                        showAlertOneButton("Please select a PNG or JPG image file.");
                    }
                }
            } else if (response == removeButton) {
                // Логика удаления аватара (установка стандартного)
                try {
                    // Устанавливаем стандартный аватар
                    setDefaultAvatar();
                    // Обновляем базу данных, устанавливая путь к стандартному изображению
                    dbHandler.updateUserAvatar(user.getId(), "/com/example/planify/images/user.png");
                    showSuccessAlert("Profile picture has been reset to default!");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlertOneButton("Failed to reset the profile picture. Please try again.");
                }
            }
        });
    }

    // Загрузка аватара пользователя из базы данных
    private void loadUserAvatar() {
        String avatarPath = dbHandler.getUserAvatar(user.getId());

        if (avatarPath != null && !avatarPath.isEmpty()) {
            // Если у пользователя есть аватар в БД, загружаем его
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                setAvatarImage(avatarFile.toURI().toString());
                return;
            }
        }

        // Если нет аватара или файл не найден, загружаем стандартный аватар
        setDefaultAvatar();
    }

    // Установка стандартного аватара
    private void setDefaultAvatar() {
        Image image = new Image(getClass().getResourceAsStream("/com/example/planify/images/user.png"));
        circleBackAvatar.setRadius(50);
        circleBackAvatar.setFill(new ImagePattern(image));
        circleBackAvatar.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
        circleBackAvatar.setStrokeWidth(1);
    }

    private Image cropToSquareCenter(Image image) {
        double width = image.getWidth();
        double height = image.getHeight();
        double size = Math.min(width, height);
        double x = (width - size) / 2;
        double y = (height - size) / 2;

        return new WritableImage(image.getPixelReader(), (int) x, (int) y, (int) size, (int) size);
    }

    // Метод для правильной установки изображения, сохраняя пропорции
    private void setAvatarImage(String imageUrl) {
        Image image = new Image(imageUrl,
                0, 0,  // Запрашиваем оригинальный размер (0 значит использовать исходный размер)
                true,  // Сохраняем пропорции
                true); // Сглаживание для лучшего качества
        Image croppedImage = cropToSquareCenter(image);

        // Создаем ImagePattern с центрированием изображения
        ImagePattern pattern = new ImagePattern(croppedImage);

        circleBackAvatar.setRadius(50);
        circleBackAvatar.setFill(pattern);
        circleBackAvatar.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
        circleBackAvatar.setStrokeWidth(1);
    }

    private void handleAvatarHoverEnter() {
        circleBackAvatar.setEffect(new DropShadow());
    }

    private void handleAvatarHoverExit() {
        circleBackAvatar.setEffect(null);
    }

    private void showAlertOneButton(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setGraphic(null);

        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconWarning.png"));
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        ButtonType cancelButtonType = new ButtonType("Close", ButtonType.CANCEL.getButtonData());
        alert.getDialogPane().getButtonTypes().setAll(cancelButtonType);

        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setGraphic(null);

        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconSuccess.png"));
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        ButtonType okButtonType = new ButtonType("Close", ButtonType.OK.getButtonData());
        alert.getDialogPane().getButtonTypes().setAll(okButtonType);

        alert.showAndWait();
    }
}