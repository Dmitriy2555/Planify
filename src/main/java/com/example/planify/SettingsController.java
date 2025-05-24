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

    private void setupEventListeners(User user) {
        dashboardLabenSignOut.setOnMouseClicked(event -> handleSignOut());
        dashboardLabenSignOut.setOnMouseEntered(event -> dashboardLabenSignOut.setUnderline(true));
        dashboardLabenSignOut.setOnMouseExited(event -> dashboardLabenSignOut.setUnderline(false));

        User currentUser = Session.getInstance().getLoggedInUser();
        DatabaseHandler dbHandler = new DatabaseHandler(); // Создаем один экземпляр для оптимизации
        menuDashboard.setOnMouseClicked(event -> {
            int teamId = dbHandler.getTeamIdByUserId(currentUser.getId());
            if (teamId != -1) {
                openNewWindow("dashboardWindow.fxml");
                handleMenuClick(new ActionEvent(menuDashboard, null)); // Активируем кнопку только при доступе
            } else {
                String message = "Admin".equalsIgnoreCase(currentUser.getUserRole()) ?
                        "Please create a team first to open a dashboard." :
                        "Please join the team first to open on dashboard.";
                showAlertOneButton(message);
            }
        });

        menuProjects.setOnMouseClicked(event -> {
            int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId());
            if (teamId != -1) {
                ObservableList<String> projects = dbHandler.loadProjectsByTeamId(teamId);
                // Если пользователь — админ, он может открыть страницу даже без проектов
                if ("Admin".equalsIgnoreCase(currentUser.getUserRole()) || !projects.isEmpty()) {
                    openNewWindow("projectsWindow.fxml");
                    handleMenuClick(new ActionEvent(menuProjects, null)); // Активируем кнопку только при доступе
                } else {
                    // Для не-админов: если проектов нет, показываем алерт
                    showAlertOneButton("No projects available. Please ask your admin to create a project.");
                }
            } else {
                showAlertOneButton("You are not a member of any team. Join a team to work on projects.");
            }
        });

        menuTasks.setOnMouseClicked(event -> {
            int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId());
            if (teamId != -1) {
                ObservableList<String> projects = dbHandler.loadProjectsByTeamId(teamId);
                if (!projects.isEmpty()) {
                    openNewWindow("tasksWindow.fxml");
                    handleMenuClick(new ActionEvent(menuTasks, null)); // Активируем кнопку только при доступе
                } else {
                    String message = "Admin".equalsIgnoreCase(currentUser.getUserRole()) ?
                            "Please create a project first to work on tasks." :
                            "No tasks available. Please ask your admin to create a project.";
                    showAlertOneButton(message);
                }
            } else {
                String message = "Admin".equalsIgnoreCase(currentUser.getUserRole()) ?
                        "Please create a team first to work on tasks." :
                        "Please join the team first to work on tasks.";
                showAlertOneButton(message);
            }
        });

        menuTeam.setOnMouseClicked(event -> {
            openNewWindow("teamWindow.fxml");
            handleMenuClick(new ActionEvent(menuTeam, null)); // Без ограничений
        });

        menuSettings.setOnMouseClicked(event -> {
            openNewWindow("settingsWindow.fxml");
            handleMenuClick(new ActionEvent(menuSettings, null)); // Без ограничений
        });

        ButtonSave.setOnMouseClicked(event -> handleSaveChanges());
        ButtonChangePass.setOnMouseClicked(event -> handleChangePassword());
        ButtonDeleteAccount.setOnMouseClicked(event -> handleRemoveUser());
    }

    // Обработчик смены активного меню
    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        User currentUser = Session.getInstance().getLoggedInUser();
        int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId());

        // Проверяем, является ли кнопка одной из ограниченных и есть ли доступ
        if ((clickedButton == menuDashboard || clickedButton == menuProjects || clickedButton == menuTasks) && teamId == -1) {
            // Не меняем стиль, так как доступа нет
            return;
        }

        // Дополнительная проверка для Tasks: нужны проекты
        if (clickedButton == menuTasks) {
            ObservableList<String> projects = new DatabaseHandler().loadProjectsByTeamId(teamId);
            if (projects.isEmpty()) {
                return; // Не меняем стиль, если нет проектов
            }
        }

        // Дополнительная проверка для Projects: нужны проекты для не-админов
        if (clickedButton == menuProjects && !"Admin".equalsIgnoreCase(currentUser.getUserRole())) {
            ObservableList<String> projects = new DatabaseHandler().loadProjectsByTeamId(teamId);
            if (projects.isEmpty()) {
                return; // Не меняем стиль для не-админов, если нет проектов
            }
        }

        if (activeButton != null) {
            activeButton.getStyleClass().remove("button-menu-active");
            activeButton.getStyleClass().add("button-menu");
        }

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

        // Сбрасываем стили перед проверкой
        resetFieldStyles();

        // Проверяем, что все поля заполнены
        boolean hasEmptyFields = false;
        if (FirstNameInput.getText().trim().isEmpty()) {
            FirstNameInput.setStyle("-fx-border-color: red; -fx-border-width: 0 0 1 0;");
            hasEmptyFields = true;
        }
        if (LastNameInput.getText().trim().isEmpty()) {
            LastNameInput.setStyle("-fx-border-color: red; -fx-border-width: 0 0 1 0;");
            hasEmptyFields = true;
        }
        if (PhoneInput.getText().trim().isEmpty() || PhoneInput.getText().equals("Not specified")) {
            PhoneInput.setStyle("-fx-border-color: red; -fx-border-width: 0 0 1 0;");
            hasEmptyFields = true;
        }
        if (AddressInput.getText().trim().isEmpty() || AddressInput.getText().equals("Not specified")) {
            AddressInput.setStyle("-fx-border-color: red; -fx-border-width: 0 0 1 0;");
            hasEmptyFields = true;
        }
        if (EmailInput.getText().trim().isEmpty()) {
            EmailInput.setStyle("-fx-border-color: red; -fx-border-width: 0 0 1 0;");
            hasEmptyFields = true;
        }

        if (hasEmptyFields) {
            // Если хотя бы одно поле пустое, показываем алерт
            showAlertOneButton("All fields must be filled out.");
            return;
        }

        // Проверяем корректность формата email
        String email = EmailInput.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            EmailInput.setStyle("-fx-border-color: red; -fx-border-width: 0 0 1 0;");
            showAlertOneButton("Incorrect email format.");
            return;
        }

        if (!EmailInput.getText().trim().equals(currentUser.getUserEmail()) && dbHandler.isEmailExists(EmailInput.getText().trim())) {
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

    // Метод для сброса стилей полей
    private void resetFieldStyles() {
        FirstNameInput.setStyle("");
        LastNameInput.setStyle("");
        PhoneInput.setStyle("");
        AddressInput.setStyle("");
        EmailInput.setStyle("");
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

    private void handleRemoveUser() {
        User currentUser = Session.getInstance().getLoggedInUser();
        DatabaseHandler dbHandler = new DatabaseHandler();

        // Show confirmation dialog for deletion
        if (!showAlertTwoButton("Are you sure you want to delete your profile?")) {
            System.out.println("Deletion canceled.");
            return;
        }

        try {
            // Get the team ID of the current user
            int teamId = dbHandler.getTeamIdByUserId(currentUser.getId());

            // Check if the user is an admin
            if (currentUser.getUserRole().equalsIgnoreCase("Admin")) {
                if (teamId != -1) {
                    // Get the list of team members
                    ObservableList<User> teamMembers = dbHandler.loadTeamMembersByTeamId(teamId);
                    // Check if there are other members besides the current user
                    if (!teamMembers.isEmpty() && !(teamMembers.size() == 1 && teamMembers.get(0).getId() == currentUser.getId())) {
                        // Create a list of member names, excluding the current user
                        ObservableList<String> teamMembersNames = FXCollections.observableArrayList();
                        for (User member : teamMembers) {
                            if (member.getId() != currentUser.getId()) {
                                teamMembersNames.add(member.getFirstName() + " " + member.getLastName());
                            }
                        }

                        // If there are other members, show dialog to select a new admin
                        if (!teamMembersNames.isEmpty()) {
                            String newAdminName = showAdminSelectionDialog(teamMembersNames);
                            if (newAdminName == null) {
                                System.out.println("Deletion canceled: new administrator not selected.");
                                showAlertOneButton("New administrator not selected. Deletion canceled.");
                                return;
                            }

                            // Split name and surname
                            String[] nameParts = newAdminName.split(" ");
                            if (nameParts.length != 2) {
                                System.out.println("Deletion canceled: incorrect name format.");
                                showAlertOneButton("Incorrect name format. Deletion canceled.");
                                return;
                            }

                            String firstName = nameParts[0];
                            String lastName = nameParts[1];

                            // Find the new admin in the database
                            User newAdmin = dbHandler.getUserByFullName(firstName, lastName);
                            if (newAdmin == null) {
                                System.out.println("Deletion canceled: new administrator not found.");
                                showAlertOneButton("Failed to select a new administrator. Deletion canceled.");
                                return;
                            }

                            // Reassign admin status
                            dbHandler.updateAdminRole(newAdmin.getId());

                            // Update the team admin record
                            dbHandler.updateTeamAdmin(teamId, newAdmin.getId());

                            // Reassign tasks to the new administrator
                            List<Task> userTasks = dbHandler.getTasksByUserId(currentUser.getId());
                            for (Task task : userTasks) {
                                dbHandler.reassignTask(task.getId(), newAdmin.getId(), currentUser.getId());
                            }
                        } else {
                            // If the admin is the last member, delete all tasks and projects associated with the team
                            dbHandler.deleteTasksByTeamId(teamId);
                            dbHandler.deleteProjectsByTeamId(teamId); // Используем правильный метод
                            dbHandler.deleteTeamById(teamId);
                        }
                    }
                }
            } else {
                // For non-admins: check if there are tasks to reassign
                if (teamId != -1) {
                    List<Task> userTasks = dbHandler.getTasksByUserId(currentUser.getId());
                    if (!userTasks.isEmpty()) {
                        // Find the admin of the current team
                        User admin = dbHandler.getAdminOfTeam(teamId);
                        if (admin == null || !admin.getUserRole().equalsIgnoreCase("Admin")) {
                            showAlertOneButton("No valid administrator found for the team. Please contact support.");
                            System.out.println("No valid admin found for team ID: " + teamId);
                            return;
                        }

                        // Reassign tasks to the team admin
                        for (Task task : userTasks) {
                            dbHandler.reassignTask(task.getId(), admin.getId(), currentUser.getId());
                        }
                    }
                }
            }

            // Удаляем пользователя из команды (если он в команде)
            if (teamId != -1) {
                dbHandler.removeUserFromTeam(teamId, currentUser.getId());
            }

            // Удаляем все зависимости пользователя
            // 1. Удаляем задачи пользователя
            dbHandler.deleteTasksByUserId(currentUser.getId());
            // 2. Удаляем записи из user_contacts
            dbHandler.deleteUserContactsByUserId(currentUser.getId());

            // Удаляем пользователя из таблицы users
            boolean isDeleted = dbHandler.deleteUserById(currentUser.getId());
            if (isDeleted) {
                showSuccessAlert("Your profile has been successfully deleted.");
                System.out.println("User successfully deleted.");
                // Очищаем сессию и переходим на окно логина
                Session.getInstance().setLoggedInUser(null);
                openNewWindow("loginWindow.fxml");
            } else {
                showAlertOneButton("Failed to delete profile. Please try again.");
                System.out.println("Failed to delete user.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlertOneButton("An error occurred while deleting the profile: " + e.getMessage());
            System.out.println("Error during deletion: " + e.getMessage());
        }
    }

    private String showAdminSelectionDialog(ObservableList<String> teamMembersNames) {
        // Создаем кастомный layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER);

        // Создаем ChoiceBox с именами участников команды
        ChoiceBox<String> choiceBox = new ChoiceBox<>(teamMembersNames);
        // Устанавливаем значение по умолчанию (первый элемент, если список не пуст)
        if (!teamMembersNames.isEmpty()) {
            choiceBox.setValue(teamMembersNames.get(0));
        }
        content.getChildren().addAll(new Label("Choose a member to be the new admin"), choiceBox);

        // Создаем диалоговое окно
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select New Admin");
        dialog.getDialogPane().setContent(content);

        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/user.png"));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

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
        Alert alert = createAlert(Alert.AlertType.WARNING, "Warning", null, message);

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
            int teamId = dbHandler.getTeamIdByUserId(currentUser.getId());
            ObservableList<User> teamMembers = dbHandler.loadTeamMembersByTeamId(teamId);

            // Проверяем, есть ли другие участники в команде
            if (teamMembers.isEmpty() || (teamMembers.size() == 1 && teamMembers.get(0).getId() == currentUser.getId())) {
                showAlertOneButton("There are no other team members to assign as admin. Role change cancelled.");
                return null;
            }

            // Создаем список с именами и фамилиями участников, исключая текущего пользователя
            ObservableList<String> teamMembersNames = FXCollections.observableArrayList();
            for (User member : teamMembers) {
                if (member.getId() != currentUser.getId()) { // Исключаем текущего пользователя
                    teamMembersNames.add(member.getFirstName() + " " + member.getLastName());
                }
            }

            // Если после исключения текущего пользователя список пуст, отменяем изменение роли
            if (teamMembersNames.isEmpty()) {
                showAlertOneButton("There are no other team members to assign as admin. Role change cancelled.");
                return null;
            }

            // Показываем диалоговое окно с выбором нового администратора
            String newAdminName = showAdminSelectionDialog(teamMembersNames);
            if (newAdminName == null) {
                // Если новый администратор не выбран, отменяем изменение роли
                showAlertOneButton("No new admin selected. Role change cancelled.");
                return null;
            }

            // Разделяем имя и фамилию
            String[] nameParts = newAdminName.split(" ");
            if (nameParts.length != 2) {
                showAlertOneButton("Invalid name format. Please try again.");
                return null;
            }

            String firstName = nameParts[0];
            String lastName = nameParts[1];

            // Находим соответствующий объект User в базе данных
            newAdmin = dbHandler.getUserByFullName(firstName, lastName);
            if (newAdmin == null) {
                showAlertOneButton("Failed to update role. Please try again.");
                return null;
            }

            // Проверяем, не является ли выбранный пользователь текущим пользователем
            if (newAdmin.getId() == currentUser.getId()) {
                showAlertOneButton("You cannot select yourself as the new admin. Role change cancelled.");
                return null;
            }

            // Переназначаем статус администратора
            dbHandler.updateAdminRole(newAdmin.getId());

            // Обновляем запись в таблице teams
            dbHandler.updateTeamAdmin(teamId, newAdmin.getId());
        }
        return newAdmin;
    }
}
