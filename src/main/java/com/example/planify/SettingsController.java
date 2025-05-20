package com.example.planify;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SettingsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane AnchorBackMenu;

    @FXML
    private Circle circleBackAvatar;

    @FXML
    private Label dashboardEmailLabel;

    @FXML
    private Label dashboardLabenSignOut;

    @FXML
    private Label dashboardNameLabel;

    @FXML
    private Button menuDashboard;

    @FXML
    private Button menuProjects;

    @FXML
    private Button menuSettings;

    @FXML
    private Button menuTasks;

    @FXML
    private Button menuTeam, ButtonSave, ButtonChangePass, ButtonDeleteAccount;

    @FXML
    private Button activeButton;

    @FXML
    private TextField FirstNameInput, LastNameInput, PhoneInput, EmailInput, AddressInput;

    @FXML
    private ChoiceBox ChoiceBoxRole;

    @FXML
    private VBox vboxAvatarNameEmail;

    @FXML
    private VBox vboxMenu;

    @FXML
    private VBox vboxSignOut;

    private AvatarHandler avatarHandler;

    @FXML
    void initialize() {
        User currentUser = Session.getInstance().getLoggedInUser();
        if (currentUser != null) {
            initializeUserData(currentUser);
            avatarHandler = new AvatarHandler(circleBackAvatar, currentUser, true);
        }

        setupUIElements();
        setupEventListeners(currentUser);
        setupChoiceBox(currentUser);
    }

    // Устанавливаем данные текущего пользователя
    private void initializeUserData(User user) {
        dashboardNameLabel.setText(user.getFirstName());
        dashboardEmailLabel.setText(user.getUserEmail());
        FirstNameInput.setText(user.getFirstName());
        LastNameInput.setText(user.getLastName());
        EmailInput.setText(user.getUserEmail());

        DatabaseHandler databaseHandler = new DatabaseHandler();
        String currentPhone = databaseHandler.getUserPhoneByUserId(user.getId());
        if(currentPhone != null)
        {
            PhoneInput.setText(currentPhone);
        }
        else
        {
            PhoneInput.setText("Not specified");
        }

        String currentAddress = databaseHandler.getUserAddressByUserId(user.getId());
        if(currentAddress != null)
        {
            AddressInput.setText(currentAddress);
        }
        else
        {
            AddressInput.setText("Not specified");
        }
    }

    /**
     * Заполняет ChoiceBox значениями ролей.
     */
    private void setupChoiceBox(User user) {
        if(user.getUserRole().equals("Admin"))
        {
            ChoiceBoxRole.getItems().addAll("Admin", "Programmer", "Tester", "Designer");
            ChoiceBoxRole.setValue("Admin"); // Значение по умолчанию
        }
        else
        {
            ChoiceBoxRole.getItems().addAll("Programmer", "Tester", "Designer");
            ChoiceBoxRole.setValue(user.getUserRole()); // Значение по умолчанию
        }
    }

    // Настраиваем UI элементы
    private void setupUIElements() {
        menuSettings.getStyleClass().add("button-menu-active");
        activeButton = menuSettings;
    }

    // Добавляем обработчики событий
    private void setupEventListeners(User user) {
        dashboardLabenSignOut.setOnMouseClicked(event -> handleSignOut());
        dashboardLabenSignOut.setOnMouseEntered(event -> dashboardLabenSignOut.setUnderline(true));
        dashboardLabenSignOut.setOnMouseExited(event -> dashboardLabenSignOut.setUnderline(false));

        menuProjects.setOnMouseClicked(event -> openNewWindow("projectsWindow.fxml"));
        menuTeam.setOnMouseClicked(event -> openNewWindow("teamWindow.fxml"));
        menuTasks.setOnMouseClicked(event -> openNewWindow("tasksWindow.fxml"));
        menuDashboard.setOnMouseClicked(event -> openNewWindow("dashboardWindow.fxml"));

        ButtonSave.setOnMouseClicked(event -> handleSaveChanges());
        ButtonChangePass.setOnMouseClicked(event -> handleChangePassword());
        ButtonDeleteAccount.setOnMouseClicked(event -> handleRemoveUser());
    }

    // Обработчик смены активного меню
    @FXML
    private void handleMenuClick(ActionEvent event) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("button-menu-active");
            activeButton.getStyleClass().add("button-menu");
        }

        Button clickedButton = (Button) event.getSource();
        clickedButton.getStyleClass().remove("button-menu");
        clickedButton.getStyleClass().add("button-menu-active");

        activeButton = clickedButton;
    }

    // Открытие нового окна
    @FXML
    public void openNewWindow(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) dashboardLabenSignOut.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setTitle("Planify");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error opening window!");
        }
    }

    // Выход из аккаунта
    @FXML
    public void handleSignOut() {
        Session.getInstance().clearSession();
        openNewWindow("loginWindow.fxml");
    }

    @FXML
    private void handleSaveChanges() {
        User currentUser = Session.getInstance().getLoggedInUser();
        DatabaseHandler dbHandler = new DatabaseHandler();

        // Проверяем, что все поля заполнены
        if (FirstNameInput.getText().isEmpty() ||
                LastNameInput.getText().isEmpty() ||
                ChoiceBoxRole.getValue() == null ||
                PhoneInput.getText().isEmpty() ||
                AddressInput.getText().isEmpty() ||
                EmailInput.getText().isEmpty() || // Добавляем проверку для EmailInput
                PhoneInput.getText().equals("Not specified") ||
                AddressInput.getText().equals("Not specified")) {
            // Если хотя бы одно поле пустое, показываем алерт
            showAlertOneButton("All fields must be filled out.");
            return;
        }

        if (!EmailInput.getText().equals(currentUser.getUserEmail()) && dbHandler.isEmailExists(EmailInput.getText())) {
            showAlertOneButton("This email is already in use.");
            return;
        }

        // Проверяем, изменилась ли роль пользователя
        String newRole = ChoiceBoxRole.getValue().toString();
        if (currentUser.getUserRole().equalsIgnoreCase("Admin") && !newRole.equalsIgnoreCase("Admin")) {
            User newAdmin = selectNewAdminIfNeeded(currentUser, newRole);
            if (newAdmin == null) {
                // Если новый администратор не выбран, отменяем изменение роли
                return;
            }
        }

        // Обновляем данные пользователя
        currentUser.setFirstName(FirstNameInput.getText().trim());
        currentUser.setLastName(LastNameInput.getText().trim());
        currentUser.setUserEmail(EmailInput.getText().trim());
        currentUser.setUserRole(newRole);

        // Обновляем контактные данные пользователя
        dbHandler.setUserPhoneByUserId(currentUser.getId(), PhoneInput.getText().trim());
        dbHandler.setUserAddressByUserId(currentUser.getId(), AddressInput.getText().trim());

        // Обновляем пользователя в базе данных
        dbHandler.updateUser(currentUser);

        // Обновляем данные в интерфейсе
        initializeUserData(currentUser);
        showSuccessAlert("User data updated successfully");
    }

    @FXML
    private void handleChangePassword()
    {
        User currentUser = Session.getInstance().getLoggedInUser();
        DatabaseHandler dbHandler = new DatabaseHandler();

        TextInputDialog dialog = createTextInputDialog("Change password", "Enter old and new password:");

        // Изменяем текст на кнопках
        ButtonType addButtonType = new ButtonType("Save", ButtonType.OK.getButtonData());
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        dialog.getDialogPane().getButtonTypes().setAll(addButtonType, cancelButtonType);

        // Показываем диалоговое окно и получаем результат
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            // Получаем состояние CheckBox
            CheckBox showPasswordCheckBox = (CheckBox) dialog.getDialogPane().lookup("#showPasswordCheckBox");
            boolean isShowPassword = showPasswordCheckBox.isSelected();

            // Получаем текст из соответствующих полей
            String oldPassword = isShowPassword ?
                    ((TextField) dialog.getDialogPane().lookup("#oldPasswordTextField")).getText() :
                    ((PasswordField) dialog.getDialogPane().lookup("#oldPasswordField")).getText();

            String newPassword = isShowPassword ?
                    ((TextField) dialog.getDialogPane().lookup("#newPasswordTextField")).getText() :
                    ((PasswordField) dialog.getDialogPane().lookup("#newPasswordField")).getText();

            if (oldPassword.trim().isEmpty()) {
                showAlertOneButton("Old password cannot be empty.");
                return;
            }

            if (newPassword.trim().isEmpty()) {
                showAlertOneButton("New password cannot be empty.");
                return;
            }

            // Проверяем, правильно ли введен старый пароль
            if (!dbHandler.getPassByUserId(currentUser.getId()).equals(oldPassword)) {
                showAlertOneButton("Old password is not correct.");
                return;
            }

            // Обновляем пароль в базе данных
            dbHandler.setPassByUserId(currentUser.getId(), newPassword);
            showSuccessAlert("Password changed successfully!");
        }
    }

    @FXML
    private void handleRemoveUser() {
        User currentUser = Session.getInstance().getLoggedInUser();
        DatabaseHandler dbHandler = new DatabaseHandler();

        if (showAlertTwoButton("Are you sure you want to delete your profile?")) {
            try {
                if (currentUser.getUserRole().equalsIgnoreCase("Admin")) {
                    // Получаем список участников команды
                    ObservableList<User> teamMembers = dbHandler.loadTeamMembersByTeamId(dbHandler.getTeamIdByUserId(currentUser.getId()));
                    if (!teamMembers.isEmpty() && !(teamMembers.size() == 1 && teamMembers.get(0).getId() == currentUser.getId())) {
                        // Создаем список с именами и фамилиями участников, исключая текущего пользователя
                        ObservableList<String> teamMembersNames = FXCollections.observableArrayList();
                        for (User member : teamMembers) {
                            if (member.getId() != currentUser.getId()) {
                                teamMembersNames.add(member.getFirstName() + " " + member.getLastName());
                            }
                        }

                        // Показываем диалоговое окно с выбором нового администратора, только если есть другие участники
                        if (!teamMembersNames.isEmpty()) {
                            String newAdminName = showAdminSelectionDialog(teamMembersNames);
                            if (newAdminName != null) {
                                // Разделяем имя и фамилию
                                String[] nameParts = newAdminName.split(" ");
                                if (nameParts.length == 2) {
                                    String firstName = nameParts[0];
                                    String lastName = nameParts[1];

                                    // Находим соответствующий объект User в базе данных
                                    User newAdmin = dbHandler.getUserByFullName(firstName, lastName);

                                    if (newAdmin != null) {
                                        // Переназначаем статус администратора
                                        dbHandler.updateAdminRole(newAdmin.getId());

                                        // Обновляем запись в таблице teams
                                        int teamId = dbHandler.getTeamIdByUserId(currentUser.getId());
                                        dbHandler.updateTeamAdmin(teamId, newAdmin.getId());

                                        // Переназначаем задачи новому администратору
                                        List<Task> userTasks = dbHandler.getTasksByUserId(currentUser.getId());
                                        for (Task task : userTasks) {
                                            dbHandler.reassignTask(task.getId(), newAdmin.getId(), currentUser.getId());
                                        }
                                    } else {
                                        // Если новый администратор не найден, отменяем удаление
                                        System.out.println("Deletion canceled: new administrator not selected.");
                                        showAlertOneButton("Failed to select a new administrator. Deletion canceled.");
                                        return;
                                    }
                                } else {
                                    // Если имя и фамилия не разделены корректно, отменяем удаление
                                    System.out.println("Deletion canceled: incorrect name format.");
                                    showAlertOneButton("Incorrect name format. Deletion canceled.");
                                    return;
                                }
                            } else {
                                // Если новый администратор не выбран, отменяем удаление
                                System.out.println("Deletion canceled: new administrator not selected.");
                                showAlertOneButton("New administrator not selected. Deletion canceled.");
                                return;
                            }
                        }
                    }
                    // Если в команде только текущий пользователь или она пуста, ничего дополнительно не делаем
                } else {
                    // Получаем администратора команды
                    User admin = dbHandler.getAdminOfTeam();
                    if (admin != null) {
                        // Переназначаем задачи администратору
                        List<Task> userTasks = dbHandler.getTasksByUserId(currentUser.getId());
                        for (Task task : userTasks) {
                            dbHandler.reassignTask(task.getId(), admin.getId(), currentUser.getId());
                        }
                    }
                }

                // Удаляем записи из user_contacts, связанные с пользователем
                dbHandler.deleteUserContactsByUserId(currentUser.getId());

                // Удаляем пользователя
                boolean isDeleted = dbHandler.deleteUserById(currentUser.getId());

                if (isDeleted) {
                    showSuccessAlert("Your profile has been successfully deleted.");
                    System.out.println("User successfully deleted.");
                    openNewWindow("loginWindow.fxml");
                } else {
                    showAlertOneButton("Failed to delete profile. Please try again.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlertOneButton("An error occurred while deleting the profile: " + e.getMessage());
            }
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    private String showAdminSelectionDialog(ObservableList<String> teamMembersNames) {
        // Создаем кастомный layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER); // Выравнивание содержимого VBox по центру

        // Создаем ChoiceBox с именами участников команды
        ChoiceBox<String> choiceBox = new ChoiceBox<>(teamMembersNames);
        content.getChildren().addAll(new Label("Choice a member"), choiceBox);

        // Создаем диалоговое окно
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select New Admin");
        dialog.getDialogPane().setContent(content);

        // Добавляем кнопки OK и Cancel
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Показываем диалоговое окно и ждем ответа
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            return choiceBox.getValue();
        }
        return null;
    }

    //диалоговое окно
    private TextInputDialog createTextInputDialog(String title, String header) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().setGraphic(null); // Убираем иконку знака вопроса

        // Создаем контейнеры для полей ввода паролей
        StackPane oldPasswordContainer = new StackPane();
        StackPane newPasswordContainer = new StackPane();

        // Создаем поля для ввода паролей
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Old password:");
        oldPasswordField.setId("oldPasswordField");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password:");
        newPasswordField.setId("newPasswordField");

        TextField oldPasswordTextField = new TextField();
        oldPasswordTextField.setPromptText("Old password:");
        oldPasswordTextField.setId("oldPasswordTextField");
        oldPasswordTextField.setVisible(false);

        TextField newPasswordTextField = new TextField();
        newPasswordTextField.setPromptText("New password:");
        newPasswordTextField.setId("newPasswordTextField");
        newPasswordTextField.setVisible(false);

        oldPasswordContainer.getChildren().addAll(oldPasswordField, oldPasswordTextField);
        newPasswordContainer.getChildren().addAll(newPasswordField, newPasswordTextField);

        // Создаем CheckBox для переключения отображения пароля
        CheckBox showPasswordCheckBox = new CheckBox("Show password");
        showPasswordCheckBox.setId("showPasswordCheckBox"); // Добавляем ID для поиска
        showPasswordCheckBox.setOnAction(e -> {
            boolean selected = showPasswordCheckBox.isSelected();
            oldPasswordField.setVisible(!selected);
            oldPasswordTextField.setVisible(selected);
            newPasswordField.setVisible(!selected);
            newPasswordTextField.setVisible(selected);

            // Синхронизируем текст между полями
            if (selected) {
                oldPasswordTextField.setText(oldPasswordField.getText());
                newPasswordTextField.setText(newPasswordField.getText());
            } else {
                oldPasswordField.setText(oldPasswordTextField.getText());
                newPasswordField.setText(newPasswordTextField.getText());
            }
        });

        // Собираем кастомный layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("Old password:"), oldPasswordContainer,
                new Label("New password:"), newPasswordContainer,
                showPasswordCheckBox
        );

        dialog.getDialogPane().setContent(content);

        // Устанавливаем иконку для окна диалога
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconChangePass.png"));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        return dialog;
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

    private boolean showAlertTwoButton(String message) {
        Alert alert = createAlert(Alert.AlertType.WARNING, "Warning", null, message);

        // Устанавливаем иконку для окна диалога
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconWarning.png"));
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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

    //админ меняет роль и передает другому пользователю
    private User selectNewAdminIfNeeded(User currentUser, String newRole) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        User newAdmin = null;

        if (!newRole.equalsIgnoreCase("Admin")) {
            // Получаем список участников команды
            ObservableList<User> teamMembers = dbHandler.loadTeamMembersByTeamId(dbHandler.getTeamIdByUserId(currentUser.getId()));
            if (!teamMembers.isEmpty()) {
                // Создаем список с именами и фамилиями участников
                ObservableList<String> teamMembersNames = FXCollections.observableArrayList();
                for (User member : teamMembers) {
                    teamMembersNames.add(member.getFirstName() + " " + member.getLastName());
                }

                // Показываем диалоговое окно с выбором нового администратора
                String newAdminName = showAdminSelectionDialog(teamMembersNames);
                if (newAdminName != null) {
                    // Разделяем имя и фамилию
                    String[] nameParts = newAdminName.split(" ");
                    if (nameParts.length == 2) {
                        String firstName = nameParts[0];
                        String lastName = nameParts[1];

                        // Находим соответствующий объект User в базе данных
                        newAdmin = dbHandler.getUserByFullName(firstName, lastName);

                        if (newAdmin != null) {
                            // Переназначаем статус администратора
                            dbHandler.updateAdminRole(newAdmin.getId());

                            // Обновляем запись в таблице teams
                            int teamId = dbHandler.getTeamIdByUserId(currentUser.getId());
                            dbHandler.updateTeamAdmin(teamId, newAdmin.getId());
                        } else {
                            // Если новый администратор не найден, отменяем изменение роли
                            showAlertOneButton("Failed to update role. Please try again.");
                            return null;
                        }
                    } else {
                        // Если имя и фамилия не разделены корректно, отменяем изменение роли
                        showAlertOneButton("Invalid name format. Please try again.");
                        return null;
                    }
                } else {
                    // Если новый администратор не выбран, отменяем изменение роли
                    showAlertOneButton("No new admin selected. Role change cancelled.");
                    return null;
                }
            }
        }
        return newAdmin;
    }
}
