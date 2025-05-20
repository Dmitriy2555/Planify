package com.example.planify;

import Animations.Shake;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button loginContinueButton;

    @FXML
    private TextField loginField;

    @FXML
    private Label loginLabel, labelPasswordForgot;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label registerLabel;

    @FXML
    void initialize() {
        setupRegisterLabel(); // Настройка событий для надписи "Зарегистрироваться"
        setupLoginButton(); // Настройка кнопки "Войти"

        labelPasswordForgot.setOnMouseEntered(event -> labelPasswordForgot.setUnderline(true));
        labelPasswordForgot.setOnMouseExited(event -> labelPasswordForgot.setUnderline(false));
        labelPasswordForgot.setOnMouseClicked(event -> handleForgotPassword());
    }

    // Настраиваем события для registerLabel (подчеркивание и переход на регистрацию)
    private void setupRegisterLabel() {
        registerLabel.setOnMouseClicked(event -> openNewWindow("registerWindow.fxml"));
        registerLabel.setOnMouseEntered(event -> registerLabel.setUnderline(true));
        registerLabel.setOnMouseExited(event -> registerLabel.setUnderline(false));
    }

    // Настраиваем кнопку входа
    private void setupLoginButton() {
        loginContinueButton.setOnAction(event -> {
            String loginText = loginField.getText().trim();
            String passwordText = passwordField.getText().trim();

            loginField.setTooltip(null);
            passwordField.setTooltip(null);

            boolean hasError = false;

            DatabaseHandler db = new DatabaseHandler();
            if (loginText.isEmpty()) {
                new Shake(loginField).playAnim();
                showErrorTooltip("Login cannot be empty", loginField);
                hasError = true;
            }
            else if (!db.isEmailExists(loginText))
            {
                new Shake(loginField).playAnim();
                showErrorTooltip("Login does not exist", loginField);
                hasError = true;
            }

            if (passwordText.isEmpty()) {
                new Shake(passwordField).playAnim();
                showErrorTooltip("Password cannot be empty", passwordField);
                hasError = true;
            }

            if (!hasError) {
                loginUser(loginText, passwordText);
            }
        });
    }

    // Проверяем, что поля не пустые
    private boolean validateInput(String loginText, String passwordText) {
        return !loginText.isEmpty() && !passwordText.isEmpty();
    }

    /**
     * Показывает сообщение об ошибке, если email уже используется.
     */
    private void showErrorTooltip(String warnung, TextField textField) {
        Tooltip tooltip = new Tooltip(warnung);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red;");

        textField.setTooltip(tooltip);
    }

    // Логика входа в систему
    private void loginUser(String loginText, String passwordText) {
        DatabaseHandler dbHandler = new DatabaseHandler();

        // 1. Проверяем, существует ли логин
        if (!dbHandler.isEmailExists(loginText)) {
            new Shake(loginField).playAnim();
            showErrorTooltip("Login does not exist", loginField);
            return;
        }

        // 2. Проверяем логин + пароль
        User user = new User(loginText, passwordText);

        try (ResultSet result = dbHandler.getUser(user)) {
            if (result.next()) {
                User loggedInUser = createUserFromResult(result);
                Session.getInstance().setLoggedInUser(loggedInUser);
                openNewWindow("dashboardWindow.fxml");
            } else {
                // Логин существует, но пароль не совпал
                new Shake(passwordField).playAnim();
                showErrorTooltip("Incorrect password", passwordField);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database error during login");
        }
    }

    // Создаем объект User из данных базы
    private User createUserFromResult(ResultSet result) throws SQLException {
        User user = new User();
        user.setId(result.getInt("id"));
        user.setFirstName(result.getString("firstName"));
        user.setLastName(result.getString("lastName"));
        user.setUserEmail(result.getString("email"));
        user.setUserPassword(result.getString("password"));
        user.setUserGender(result.getString("gender"));
        user.setUserRole(result.getString("role"));
        return user;
    }

    // Открываем новое окно (используется для входа и регистрации)
    @FXML
    public void openNewWindow(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) loginContinueButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle("Planify");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error opening window!");
        }
    }

    @FXML
    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Enter your email to receive your password:");
        dialog.setGraphic(null);
        dialog.getEditor().setPromptText("Email");

        // Изменяем текст на кнопках
        ButtonType addButtonType = new ButtonType("Send", ButtonType.OK.getButtonData());
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        dialog.getDialogPane().getButtonTypes().setAll(addButtonType, cancelButtonType);

        //Устанавливаем иконку окна сброса пароля
        // Устанавливаем иконку для окна диалога
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconChangePass.png"));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (email.trim().isEmpty()) {
                showAlertOneButton("Email cannot be empty.");
                return;
            }

            DatabaseHandler dbHandler = new DatabaseHandler();
            String password = dbHandler.getPasswordByEmail(email.trim());

            if (password == null) {
                showAlertOneButton("Email does not exist.");
            } else {
                //Отправка сообщения пользователю на почту
                EmailSender emailSender = new EmailSender();
                User user = dbHandler.getUserByEmail(email.trim());
                emailSender.EmailSend(email, user.getFirstName(), "Your password is: " + password + "\nDon't forget your password!");

                showSuccessAlert("Password has been sent to your email.");
            }
        });
    }

    private Alert createAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.getDialogPane().setGraphic(null);

        return alert;
    }

    private void showSuccessAlert(String message) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, "Success", null, message);

        // Устанавливаем иконку для окна диалога (галочка)
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconSuccess.png"));
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        // Изменяем текст на кнопках
        ButtonType okButtonType = new ButtonType("Close", ButtonType.OK.getButtonData());
        alert.getDialogPane().getButtonTypes().setAll(okButtonType);

        alert.showAndWait();
    }

    private void showAlertOneButton(String message) {
        Alert alert = createAlert(Alert.AlertType.WARNING, "Error", null, message);

        // Устанавливаем иконку для окна диалога
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconWarning.png"));
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        // Изменяем текст на кнопках
        ButtonType cancelButtonType = new ButtonType("Close", ButtonType.CANCEL.getButtonData());
        alert.getDialogPane().getButtonTypes().setAll(cancelButtonType);

        alert.showAndWait();
    }
}
