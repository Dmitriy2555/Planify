package com.example.planify;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import Animations.Shake;
import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TeamController {

    // UI элементы из FXML
    @FXML private AnchorPane AnchorBackMenu;
    @FXML private TableView<User> adminTableTeam;
    @FXML private TableColumn<User, Void> columnAction;
    @FXML private TableColumn<User, String> columnEmail;
    @FXML private TableColumn<User, String> columnName;
    @FXML private TableColumn<User, String> columnRole;
    @FXML private Label dashboardEmailLabel;
    @FXML private Label dashboardLabenSignOut;
    @FXML private Label dashboardNameLabel, teamIdResultat, teamName;
    @FXML private Button menuDashboard, menuProjects, menuSettings, menuTasks, menuTeam;
    @FXML private Button teamButtonRemoveUser, teamButtonJoinTeam, teamButtonLeaveTeam;
    @FXML private VBox vboxAvatarNameEmail, vboxMenu, vboxSignOut;
    @FXML private Circle circleBackAvatar;
    @FXML private Button activeButton, teamButtonAddUser, createTeamButton;
    @FXML private StackPane welcomeStackPaneBack;
    @FXML private Text welcomeLabel;


    private Team team; // Переменная для хранения команды

    private AvatarHandler avatarHandler;

    @FXML
    void initialize() {
        User currentUser = Session.getInstance().getLoggedInUser();
        if (currentUser != null) {
            initializeUserData(currentUser);
            // Убедимся, что команда загружается перед настройкой интерфейса
            if ("Admin".equalsIgnoreCase(currentUser.getUserRole())) {
                checkAndLoadTeam(currentUser); // Загружаем команду для админа
            } else {
                loadTeamForUser(currentUser); // Загружаем команду для обычного пользователя
            }
            configureInterface(currentUser); // Настраиваем интерфейс после загрузки команды
            avatarHandler = new AvatarHandler(circleBackAvatar, currentUser, false);
        }

        setupUIElements();
        setupEventListeners();

    }

    private void initializeUserData(User user) {
        dashboardNameLabel.setText(user.getFirstName());
        dashboardEmailLabel.setText(user.getUserEmail());
        System.out.println("User ID: " + user.getId());
        //setupTeamTable(user);
    }

    private void configureInterface(User user) {
        if ("Admin".equalsIgnoreCase(user.getUserRole())) {
            setupAdminInterface();
            setupTeamNameEdit();

            // Динамическое обновление видимости кнопки "Remove user" при изменении выбора в таблице
            adminTableTeam.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                updateButtonVisibilityForAdmin();
            });

            // Начальное обновление видимости
            updateButtonVisibilityForAdmin();
        } else {
            loadTeamForUser(user); // Загружаем данные команды для пользователя
            setupUserInterface(user); // Настраиваем интерфейс с учётом состояния команды
        }
    }

    private void updateButtonVisibilityForAdmin() {
        boolean hasTeam = team != null;
        boolean hasSelectedUser = adminTableTeam.getSelectionModel().getSelectedItem() != null;
        teamButtonRemoveUser.setVisible(hasTeam && hasSelectedUser);
    }

    private void updateButtonVisibilityForUser() {
        boolean hasTeam = team != null;
        teamButtonLeaveTeam.setVisible(hasTeam);
    }

    private void setupAdminInterface() {
        // Проверяем, есть ли команда у админа
        boolean hasTeam = (team != null);

        if (!hasTeam) {
            // Если команды нет: скрываем таблицу, надписи и кнопки, показываем приветствие
            adminTableTeam.setVisible(false);
            teamName.setVisible(false);
            teamIdResultat.setVisible(false);
            teamIdResultat.setText(""); // Очищаем текст, чтобы не осталось старых данных
            teamButtonAddUser.setVisible(false);
            teamButtonRemoveUser.setVisible(false);
            teamButtonJoinTeam.setVisible(false);
            teamButtonLeaveTeam.setVisible(false);
            welcomeStackPaneBack.setVisible(true);
            welcomeLabel.setVisible(true);
            createTeamButton.setVisible(true); // Показываем кнопку создания команды
        } else {
            // Если команда есть: показываем таблицу, надписи и кнопки, скрываем приветствие
            adminTableTeam.setVisible(true);
            teamName.setVisible(true);
            teamIdResultat.setVisible(true);
            teamIdResultat.setText(team.getName()); // Устанавливаем имя команды
            teamButtonAddUser.setVisible(true); // Показываем кнопку "Add User"
            teamButtonRemoveUser.setVisible(adminTableTeam.getSelectionModel().getSelectedItem() != null); // Логика видимости для "Remove User"
            teamButtonJoinTeam.setVisible(false);
            teamButtonLeaveTeam.setVisible(false);
            welcomeStackPaneBack.setVisible(false);
            welcomeLabel.setVisible(false);
            createTeamButton.setVisible(false); // Скрываем кнопку создания команды
        }

        // Настраиваем действия для кнопок
        teamButtonAddUser.setOnAction(e -> handleAddUser());
        teamButtonRemoveUser.setOnAction(e -> handleRemoveUser());
        createTeamButton.setOnAction(e -> handleCreateTeam());
    }

    private void handleCreateTeam() {
        User currentUser = Session.getInstance().getLoggedInUser();
        if (currentUser != null) {
            createNewTeam(currentUser); // Вызываем метод создания команды
            // Перезагружаем данные команды и обновляем интерфейс
            checkAndLoadTeam(currentUser); // Убедимся, что team обновлён
            setupAdminInterface(); // Перестраиваем интерфейс с новым состоянием
        }
    }

    private void setupUserInterface(User user) {
        teamButtonAddUser.setVisible(false);
        teamButtonRemoveUser.setVisible(false);
        createTeamButton.setVisible(false);

        // Проверяем, есть ли команда для пользователя
        boolean hasTeam = (team != null);
        teamButtonJoinTeam.setVisible(!hasTeam);
        teamButtonLeaveTeam.setVisible(hasTeam);
        teamIdResultat.setVisible(hasTeam);
        if (!hasTeam) {
            teamName.setVisible(false);
            teamIdResultat.setText(""); // Очищаем имя команды, если команды нет
            // Делаем таблицу невидимой, а StackPane с надписью видимым
            adminTableTeam.setVisible(false);
            welcomeStackPaneBack.setVisible(true);
            welcomeLabel.setVisible(true);
        } else {
            teamName.setVisible(true);
            teamIdResultat.setText(team.getName()); // Устанавливаем имя команды

            // Делаем таблицу видимой, а StackPane с надписью невидимым
            adminTableTeam.setVisible(true);
            welcomeStackPaneBack.setVisible(false);
            welcomeLabel.setVisible(false);
        }

        teamButtonJoinTeam.setOnAction(e -> handleJoinTeam());
        teamButtonLeaveTeam.setOnAction(e -> handleLeaveTeam());
    }

    private void setupTeamNameEdit() {
        teamIdResultat.setOnMouseEntered(event -> teamIdResultat.setUnderline(true));
        teamIdResultat.setOnMouseExited(event -> teamIdResultat.setUnderline(false));

        teamIdResultat.setOnMouseClicked(event -> {
            TextInputDialog dialog = createTextInputDialog("Edit Name", "Enter new team name:", "New name:");

            // Устанавливаем иконку для окна диалога
            Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconEdit.png"));
            Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(icon);

            // Настройка кнопок
            ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().setAll(confirmButtonType, cancelButtonType);

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                if (!newName.trim().isEmpty()) {
                    if (team == null) {
                        System.err.println("Ошибка: объект team не инициализирован");
                        return;
                    }

                    // Создаем экземпляр DatabaseHandler
                    DatabaseHandler dbHandler = new DatabaseHandler();

                    // Проверяем, существует ли уже команда с таким именем (исключая текущую команду)
                    if (dbHandler.doesTeamNameExist(newName, team.getId())) {
                        showAlertOneButton("A team with the name '" + newName + "' already exists. Please choose a different name.");
                        return;
                    }

                    teamIdResultat.setText(newName);

                    // Вызываем метод для обновления имени команды в БД
                    dbHandler.updateTeamName(team.getId(), newName);

                    // Обновляем объект team
                    team.setName(newName);

                    System.out.println("Team name changed to: " + newName);
                } else {
                    showAlertOneButton("Team name cannot be empty.");
                }
            });
        });
    }

    private void setupUIElements() {
        menuTeam.getStyleClass().add("button-menu-active");
        activeButton = menuTeam;

        adminTableTeam.getColumns().forEach(column -> {
            column.setReorderable(false);
            column.setResizable(false);
            column.setSortable(false);
        });
    }

    private void setupEventListeners() {
        dashboardLabenSignOut.setOnMouseClicked(event -> handleSignOut());
        dashboardLabenSignOut.setOnMouseEntered(event -> dashboardLabenSignOut.setUnderline(true));
        dashboardLabenSignOut.setOnMouseExited(event -> dashboardLabenSignOut.setUnderline(false));

        User currentUser = Session.getInstance().getLoggedInUser();
        menuDashboard.setOnMouseClicked(event -> {
            int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId());
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
                DatabaseHandler dbHandler = new DatabaseHandler();
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
                DatabaseHandler dbHandler = new DatabaseHandler();
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
    }

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

    @FXML
    public void handleSignOut() {
        Session.getInstance().clearSession();
        openNewWindow("loginWindow.fxml");
    }

    @FXML
    private void handleAddUser() {
        //Проверка, если вообще команда перед добавлением учасников
        if (team == null) {
            showAlertOneButton("Please create a team and set a name before adding members.");
            return;
        }
        TextInputDialog dialog = createTextInputDialog("Add User", "Enter the user's email to add:", "Email:");

        // Изменяем текст на кнопках
        ButtonType addButtonType = new ButtonType("Add", ButtonType.OK.getButtonData());
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        dialog.getDialogPane().getButtonTypes().setAll(addButtonType, cancelButtonType);

        dialog.showAndWait().ifPresent(email -> {
            if (email.trim().isEmpty()) {
                showAlertOneButton("Email cannot be empty.");
                return;
            }

            DatabaseHandler dbHandler = new DatabaseHandler();
            User user = dbHandler.getUserByEmail(email);

            if (user == null) {
                showAlertOneButton("User with email \"" + email + "\" not found.");
            } else if (isUserInTable(user)) {
                showAlertOneButton("User with email \"" + email + "\" is already added to the table.");
            }
            else if (user.getUserRole().equalsIgnoreCase("Admin")) {
                showAlertOneButton("Cannot add a user with 'Admin' role to the team.");
            }
            else {
                adminTableTeam.getItems().add(user);
                adminTableTeam.refresh();
                System.out.println("Добавлен: " + user.getFirstName());

                // Обрезаем роль до нужной длины
                String role = user.getUserRole();
                if (role.length() > 50) {
                    role = role.substring(0, 50);
                }

                // Добавляем пользователя в команду в базе данных
                dbHandler.addUserToTeam(team.getId(), user.getId(), user.getUserRole());
                showSuccessAlert(user.getFirstName() + " added to team");
                System.out.println("User added to team in the database.");

                //отправка сообщения на почту, после добавления в команду
                EmailSender emailSender = new EmailSender();
                String emailAssignee = email.trim();
                String firstName = user.getFirstName();
                String nameOfTeam = teamIdResultat.getText();
                emailSender.EmailSend(emailAssignee, firstName, "You have been added to a new team.\nTeam name: " + nameOfTeam + "\nCheck your app!");
            }
        });
    }

    @FXML
    private void handleRemoveUser() {
        User selectedUser = adminTableTeam.getSelectionModel().getSelectedItem();
        User currentUser = Session.getInstance().getLoggedInUser();

        // Проверка на удаление самого себя (на всякий случай)
        if (selectedUser.getUserEmail().equals(currentUser.getUserEmail())) {
            System.out.println("You cannot remove yourself from the team.");
            showAlertOneButton("You cannot remove yourself from the team.");
            return;
        }

        if (showAlertTwoButton("Are you sure you want to remove " + selectedUser.getFirstName() + "?")) {
            // Удаляем пользователя из таблицы
            adminTableTeam.getItems().remove(selectedUser);
            adminTableTeam.refresh();
            System.out.println("User removed from table: " + selectedUser.getFirstName());

            // Удаляем пользователя из команды в базе данных
            DatabaseHandler dbHandler = new DatabaseHandler();
            dbHandler.removeUserFromTeam(team.getId(), selectedUser.getId());
            showSuccessAlert(selectedUser.getFirstName() + " removed from team");
            System.out.println("User removed from team in the database.");

            // Отправляем сообщение на почту
            EmailSender emailSender = new EmailSender();
            String toEmail = selectedUser.getUserEmail();
            String firstName = selectedUser.getFirstName();
            String nameOfTeam = teamIdResultat.getText();
            emailSender.EmailSend(toEmail, firstName, "You have been removed from team.\nTeam name: " + nameOfTeam + "\nCheck your app!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    @FXML
    private void handleJoinTeam() {
        TextInputDialog dialog = createTextInputDialog("Join Team", "Enter the team name to join:", "Team Name:");

        // Изменяем текст на кнопках
        ButtonType joinButtonType = new ButtonType("Join", ButtonType.OK.getButtonData());
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        dialog.getDialogPane().getButtonTypes().setAll(joinButtonType, cancelButtonType);

        dialog.showAndWait().ifPresent(teamName -> {
            if (teamName.trim().isEmpty()) {
                showAlertOneButton("Team name cannot be empty.");
                return;
            }

            DatabaseHandler dbHandler = new DatabaseHandler();
            if (dbHandler.isTeamExists(teamName)) {
                User currentUser = Session.getInstance().getLoggedInUser();
                dbHandler.addUserToTeam(dbHandler.getTeamIdByName(teamName), currentUser.getId(), currentUser.getUserRole());
                loadTeamForUser(currentUser); // Обновляем интерфейс после присоединения к команде
                setupUserInterface(currentUser); // Обновляем видимость кнопок
                showSuccessAlert("You have successfully joined the team: " + teamName);

                // Отправляем сообщение на почту
                EmailSender emailSender = new EmailSender();
                String firstName = currentUser.getFirstName();
                String toEmail = currentUser.getUserEmail();
                emailSender.EmailSend(toEmail, firstName, "You joined a new team.\nTeam name: " + teamName + "\nLet's work together!");
            } else {
                showAlertOneButton("Team with name \"" + teamName + "\" does not exist.");
            }
        });
    }

    @FXML
    private void handleLeaveTeam() {
        if (showAlertTwoButton("Are you sure you want to leave the team?")) {
            User currentUser = Session.getInstance().getLoggedInUser();
            String nameOfTeam = teamIdResultat.getText();
            DatabaseHandler dbHandler = new DatabaseHandler();
            dbHandler.removeUserFromTeam(team.getId(), currentUser.getId());
            loadTeamForUser(currentUser); // Обновляем интерфейс после удаления из команды
            setupUserInterface(currentUser); // Обновляем видимость кнопок
            showSuccessAlert("You have successfully left the team.");

            // Отправляем сообщение на почту
            EmailSender emailSender = new EmailSender();
            String firstName = currentUser.getFirstName();
            String toEmail = currentUser.getUserEmail();
            emailSender.EmailSend(toEmail, firstName, "You leaved your team.\nTeam name: " + nameOfTeam + "\nGood luck!");
        }
        System.out.println("User left the team.");
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

    private Alert createAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.getDialogPane().setGraphic(null);

        return alert;
    }

    private TextInputDialog createTextInputDialog(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        dialog.getDialogPane().setGraphic(null);

        // Устанавливаем иконку для окна диалога
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/user.png"));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(icon);

        return dialog;
    }

    private void setupTeamTable(User currentUser) {
        ObservableList<User> userList = FXCollections.observableArrayList();

        if (team != null) {
            userList.addAll(loadTeamMembers(team.getId()));

            // Сортируем список, чтобы админ был первым
            FXCollections.sort(userList, (u1, u2) -> {
                boolean isAdmin1 = "Admin".equalsIgnoreCase(u1.getUserRole());
                boolean isAdmin2 = "Admin".equalsIgnoreCase(u2.getUserRole());
                return Boolean.compare(isAdmin2, isAdmin1); // Админ будет первым
            });
        } else {
            System.err.println("Ошибка: объект team не инициализирован");
        }

        columnName.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user != null ? user.getFirstName() + " " + user.getLastName() : "");
        });

        columnRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserRole()));
        columnEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserEmail()));

        adminTableTeam.setItems(userList);
    }

    //для админов
    private void checkAndLoadTeam(User currentUser) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        String query = "SELECT * FROM " + Constant.TEAM_TABLE + " WHERE " + Constant.TEAM_ADMIN_ID + " = ?";
        try (PreparedStatement pstmt = dbHandler.getDbConnection().prepareStatement(query)) {
            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int teamId = rs.getInt(Constant.TEAM_ID);
                String teamName = rs.getString(Constant.TEAM_NAME);
                team = new Team(teamId, teamName, currentUser.getId()); // Инициализация объекта team
                teamIdResultat.setText(teamName);
                System.out.println("Team loaded: " + teamName);

                // Вызываем setupTeamTable после успешной загрузки команды
                setupTeamTable(currentUser);
            } else {
                System.out.println("No team found for the current user.");
                teamIdResultat.setText("Enter id");
                teamIdResultat.setOnMouseClicked(event -> createNewTeam(currentUser));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //для обычных пользователей
    private void loadTeamForUser(User currentUser) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        String query = "SELECT t.* FROM " + Constant.TEAM_TABLE + " t " +
                "JOIN " + Constant.TEAM_MEMBERS_TABLE + " tm ON t." + Constant.TEAM_ID + " = tm." + Constant.TEAM_MEMBERS_TEAM_ID +
                " WHERE tm." + Constant.TEAM_MEMBERS_USER_ID + " = ?";
        try (PreparedStatement pstmt = dbHandler.getDbConnection().prepareStatement(query)) {
            pstmt.setInt(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int teamId = rs.getInt(Constant.TEAM_ID);
                String teamName = rs.getString(Constant.TEAM_NAME);
                team = new Team(teamId, teamName, rs.getInt(Constant.TEAM_ADMIN_ID)); // Инициализация объекта team
                teamIdResultat.setText(teamName);
                System.out.println("Team loaded: " + teamName);

                // Вызываем setupTeamTable после успешной загрузки команды
                setupTeamTable(currentUser);
            } else {
                System.out.println("No team found for the current user.");
                teamIdResultat.setText("No team");
                team = null; // Очищаем объект team
                setupTeamTable(currentUser); // Очищаем таблицу
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<User> loadTeamMembers(int teamId) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        String query = "SELECT u.* FROM " + Constant.TEAM_MEMBERS_TABLE + " tm " +
                "JOIN " + Constant.USER_TABLE + " u ON tm." + Constant.TEAM_MEMBERS_USER_ID + " = u." + Constant.USER_ID +
                " WHERE tm." + Constant.TEAM_MEMBERS_TEAM_ID + " = ?";
        ObservableList<User> userList = FXCollections.observableArrayList();

        try (PreparedStatement pstmt = dbHandler.getDbConnection().prepareStatement(query)) {
            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = new User(
                        rs.getString(Constant.USER_FIRSTNAME),
                        rs.getString(Constant.USER_LASTNAME),
                        rs.getString(Constant.USER_EMAIL),
                        rs.getString(Constant.USER_PASS),
                        rs.getString(Constant.USER_GENDER),
                        rs.getString(Constant.USER_ROLE)
                );
                user.setId(rs.getInt(Constant.USER_ID));

                // Проверка на дублирование
                boolean alreadyExists = userList.stream()
                        .anyMatch(existingUser -> existingUser.getId() == user.getId());

                if (!alreadyExists) {
                    userList.add(user);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return userList;
    }

    private void createNewTeam(User currentUser) {
        TextInputDialog dialog = createTextInputDialog("Создать команду", "Enter the new team name:", "Team name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(teamName -> {
            if (!teamName.trim().isEmpty()) {
                int adminId = currentUser.getId();
                System.out.println("Creating team with name: " + teamName + ", admin ID: " + adminId);

                DatabaseHandler dbHandler = new DatabaseHandler();
                try {
                    dbHandler.createTeam(teamName, adminId);
                    teamIdResultat.setText(teamName);
                    System.out.println("Team created: " + teamName);

                    checkAndLoadTeam(currentUser);

                    if (team != null) {
                        dbHandler.addUserToTeam(team.getId(), adminId, currentUser.getUserRole());
                        setupTeamTable(currentUser);
                        adminTableTeam.refresh();
                        showSuccessAlert("Team successfully created");
                        System.out.println("Admin added to the team.");
                    } else {
                        System.err.println("Team object not initialized");
                    }
                } catch (SQLException e) {
                    showAlertOneButton("Team name '" + teamName + "' already exists.");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                showAlertOneButton("Team name cannot be empty.");
            }
        });
    }

    private boolean isUserInTable(User user) {
        return adminTableTeam.getItems().stream()
                .anyMatch(u -> u.getUserEmail().equalsIgnoreCase(user.getUserEmail()));
    }
}
