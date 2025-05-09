package com.example.planify;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterController {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;

    @FXML
    private Label loginLabel;
    @FXML
    private Button registerContinueButton;
    @FXML
    private TextField registerEmailField;
    @FXML
    private ImageView registerIconUser;
    @FXML
    private Label registerLabel;
    @FXML
    private TextField registerNameField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private ChoiceBox<String> registerChoiceBoxRole;
    @FXML
    private TextField registerFirstNameField;
    @FXML
    private TextField registerLastNameField;
    @FXML
    private RadioButton registerRadioButtonMale;
    @FXML
    private RadioButton registerRadioButtonFemale;

    private final DatabaseHandler dbHandler = new DatabaseHandler();
    private final ToggleGroup genderGroup = new ToggleGroup();

    @FXML
    void initialize() {
        setupLoginLabel();
        setupChoiceBox();
        setupRadioButtons();
        registerContinueButton.setOnAction(event -> signUpNewUser());
    }

    /**
     * Настройка перехода на окно логина при нажатии на loginLabel.
     */
    private void setupLoginLabel() {
        loginLabel.setOnMouseClicked(event -> openNewWindow("loginWindow.fxml"));
        loginLabel.setOnMouseEntered(event -> loginLabel.setUnderline(true));
        loginLabel.setOnMouseExited(event -> loginLabel.setUnderline(false));
    }

    /**
     * Заполняет ChoiceBox значениями ролей.
     */
    private void setupChoiceBox() {
        registerChoiceBoxRole.getItems().addAll("Admin", "Programmer", "Tester", "Designer");
        registerChoiceBoxRole.setValue("Admin"); // Значение по умолчанию
    }

    /**
     * Группировка радио-кнопок для выбора пола.
     */
    private void setupRadioButtons() {
        registerRadioButtonMale.setToggleGroup(genderGroup);
        registerRadioButtonFemale.setToggleGroup(genderGroup);
        registerRadioButtonMale.setSelected(true);
    }

    /**
     * Открывает указанное FXML-окно.
     */
    @FXML
    public void openNewWindow(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) registerContinueButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle("Planify");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error opening window!");
        }
    }

    /**
     * Регистрирует нового пользователя после проверки данных.
     */
    private void signUpNewUser() {
        resetFieldStyles(); // Сброс стилей полей

        String firstName = registerFirstNameField.getText().trim();
        String lastName = registerLastNameField.getText().trim();
        String userEmail = registerEmailField.getText().trim();
        String userPassword = registerPasswordField.getText().trim();
        String userGender = ((RadioButton) genderGroup.getSelectedToggle()).getText();
        String userRole = registerChoiceBoxRole.getValue();

        boolean isValid = validateFields(firstName, lastName, userEmail, userPassword);

        if (isValid && dbHandler.isEmailExists(userEmail)) {
            showEmailErrorTooltip("This email already used");
            isValid = false;
        }
        if (!userEmail.contains("@") || !userEmail.contains("."))
        {
            showEmailErrorTooltip("Invalid email address");
            isValid = false;
        }
        if (isValid) {
            User user = new User(firstName, lastName, userEmail, userPassword, userGender, userRole);
            dbHandler.signUpUser(user);
            openNewWindow("loginWindow.fxml"); // Переход на страницу логина
        }
    }

    /**
     * Проверяет, что все обязательные поля заполнены.
     */
    private boolean validateFields(String firstName, String lastName, String email, String password) {
        boolean isValid = true;

        if (firstName.isEmpty()) {
            registerFirstNameField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        if (lastName.isEmpty()) {
            registerLastNameField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        if (email.isEmpty()) {
            registerEmailField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        if (password.isEmpty()) {
            registerPasswordField.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        if (!isValid) {
            System.out.println("Ошибка: все поля должны быть заполнены!");
        }

        return isValid;
    }

    /**
     * Показывает сообщение об ошибке, если email уже используется.
     */
    private void showEmailErrorTooltip(String warnung) {
        registerEmailField.setStyle("-fx-border-color: red;");

        Tooltip tooltip = new Tooltip(warnung);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red;");

        registerEmailField.setTooltip(tooltip);
    }

    /**
     * Сбрасывает стили всех полей ввода.
     */
    private void resetFieldStyles() {
        registerFirstNameField.setStyle(null);
        registerLastNameField.setStyle(null);
        registerEmailField.setStyle(null);
        registerPasswordField.setStyle(null);
    }
}
