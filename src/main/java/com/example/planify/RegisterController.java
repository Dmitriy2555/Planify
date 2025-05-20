package com.example.planify;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Animations.Shake;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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

        // Проверяем поля как на пустую строку, так и на наличие в базе данных
        if (!validateFields(firstName, lastName, userEmail, userPassword)) {
            return;
        }

        User user = new User(firstName, lastName, userEmail, userPassword, userGender, userRole);
        dbHandler.signUpUser(user);

        showSuccessAlert("You have successfully registered");

        EmailSender emailSender = new EmailSender();
        emailSender.EmailSend(userEmail, firstName, "Welcome to Planify.\nYou have successfully registered.");

        openNewWindow("loginWindow.fxml");
    }

    /**
     * Проверяет, что все обязательные поля заполнены.
     */
    private boolean validateFields(String firstName, String lastName, String email, String password) {
        boolean isValid = true;

        if (firstName.isEmpty()) {
            showErrorTooltip("First name cannot be empty", registerFirstNameField);
            isValid = false;
        }
        if (lastName.isEmpty()) {
            showErrorTooltip("Last name cannot be empty", registerLastNameField);
            isValid = false;
        }

        if (email.isEmpty()) {
            showErrorTooltip("Email cannot be empty", registerEmailField);
            isValid = false;
        }
        else if (dbHandler.isEmailExists(email)) {
            showErrorTooltip("This email is already in use", registerEmailField);
            isValid = false;
        }
        else if (!email.contains("@") || !email.contains("."))
        {
            showErrorTooltip("Incorrect mail format", registerEmailField);
            isValid = false;
        }

        if (password.isEmpty()) {
            showErrorTooltip("Passwort cannot be empty", registerPasswordField);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Показывает сообщение об ошибке, если email уже используется.
     */
    private void showErrorTooltip(String warnung, TextField textField) {
        new Shake(textField).playAnim();

        Tooltip tooltip = new Tooltip(warnung);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red;");

        textField.setTooltip(tooltip);
    }

    /**
     * Сбрасывает стили всех полей ввода.
     */
    private void resetFieldStyles() {
        registerFirstNameField.setTooltip(null);
        registerLastNameField.setTooltip(null);
        registerEmailField.setTooltip(null);
        registerPasswordField.setTooltip(null);
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
}
