package com.example.planify;

import Animations.Shake;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    private Label loginLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label registerLabel;

    @FXML
    void initialize() {
        setupRegisterLabel(); // Настройка событий для надписи "Зарегистрироваться"
        setupLoginButton(); // Настройка кнопки "Войти"
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

            if (validateInput(loginText, passwordText)) {
                loginUser(loginText, passwordText); // Попытка входа
            } else {
                showEmptyFieldError(loginText, passwordText); // Ошибка пустых полей
            }
        });
    }

    // Проверяем, что поля не пустые
    private boolean validateInput(String loginText, String passwordText) {
        return !loginText.isEmpty() && !passwordText.isEmpty();
    }

    // Показываем ошибку, если поля пустые
    private void showEmptyFieldError(String loginText, String passwordText) {
        System.out.println("Login and Password cannot be empty");
        if (loginText.isEmpty()) new Shake(loginField).playAnim();
        if (passwordText.isEmpty()) new Shake(passwordField).playAnim();
    }

    // Показываем ошибку, если данные не найдены в базе
    private void showLoginFailedError() {
        System.out.println("Incorrect login or password");
        new Shake(loginField).playAnim();
        new Shake(passwordField).playAnim();
    }

    // Логика входа в систему
    private void loginUser(String loginText, String passwordText) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        User user = new User(loginText, passwordText);

        try (ResultSet result = dbHandler.getUser(user)) {
            if (result.next()) {
                User loggedInUser = createUserFromResult(result); // Создаем объект User
                Session.getInstance().setLoggedInUser(loggedInUser); // Сохраняем пользователя в сессии
                openNewWindow("dashboardWindow.fxml"); // Переход в личный кабинет
            } else {
                showLoginFailedError(); // Ошибка неверного логина или пароля
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
}
