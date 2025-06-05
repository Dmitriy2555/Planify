package com.example.planify;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML
    private VBox dashboardMenu;

    @FXML
    private AnchorPane backgroundProjects;

    @FXML
    private Button menuProjects, menuDashboard, menuTeam, menuTasks, menuSettings;

    @FXML
    private Label dashboardEmailLabel, firstNameLabelResultat, lastNameLabelResultat,
            emailLabelResultat, roleLabelResultat, dashboardNameLabel, dashboardLabenSignOut,
            tasksCreatedLabelResultat, overdueTasksLabelResultat, tasksCompletedLabelResultat;

    @FXML
    private ListView<String> dashboardTasksList, dashboardProjectsList;

    @FXML
    private Circle circleBackAvatar;

    @FXML
    private Button activeButton;

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;

    private AvatarHandler avatarHandler;

    @FXML
    void initialize() {
        User currentUser = Session.getInstance().getLoggedInUser();
        if (currentUser != null) {
            initializeUserData(currentUser);
            avatarHandler = new AvatarHandler(circleBackAvatar, currentUser, false);
        }

        setupUIElements();
        setupEventListeners();
        populateProjectLists(currentUser);
        setupSelectionHandlers();

        // Отключаем выбор для dashboardTasksList
        if (dashboardTasksList != null) {
            dashboardTasksList.setSelectionModel(null); // Отключаем модель выбора
        }

        // Добавляем обработчик событий для выбора проекта
        dashboardProjectsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadTasksForSelectedProject(newValue);
            }
        });
    }

    // Устанавливаем данные текущего пользователя
    private void initializeUserData(User user) {
        dashboardNameLabel.setText(user.getFirstName());
        dashboardEmailLabel.setText(user.getUserEmail());
        firstNameLabelResultat.setText(user.getFirstName());
        lastNameLabelResultat.setText(user.getLastName());
        emailLabelResultat.setText(user.getUserEmail());
        roleLabelResultat.setText(user.getUserRole());

        DatabaseHandler dbHandler = new DatabaseHandler();

        int createdTasks = dbHandler.getCreatedTaskCount(user.getId());
        int completedTasks = dbHandler.getCompletedTaskCount(user.getId());
        int overdueTasks = dbHandler.getOverdueTaskCount(user.getId());

        tasksCreatedLabelResultat.setText(String.valueOf(createdTasks));
        tasksCompletedLabelResultat.setText(String.valueOf(completedTasks));
        overdueTasksLabelResultat.setText(String.valueOf(overdueTasks));
    }

    // Настраиваем UI элементы
    private void setupUIElements() {
        menuDashboard.getStyleClass().add("button-menu-active");
        activeButton = menuDashboard;
    }

    // Добавляем обработчики событий
    private void setupEventListeners() {
        dashboardLabenSignOut.setOnMouseClicked(event -> handleSignOut());
        dashboardLabenSignOut.setOnMouseEntered(event -> dashboardLabenSignOut.setUnderline(true));
        dashboardLabenSignOut.setOnMouseExited(event -> dashboardLabenSignOut.setUnderline(false));

        User currentUser = Session.getInstance().getLoggedInUser();

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

        menuTeam.setOnMouseClicked(event -> openNewWindow("teamWindow.fxml"));

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

        menuSettings.setOnMouseClicked(event -> openNewWindow("settingsWindow.fxml"));
    }

    // Заполняем список задач и проектов
    private void populateProjectLists(User currentUser) {
        dashboardProjectsList.getItems().clear(); // Очищаем текущий список

        if (currentUser != null) {
            int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId()); // Получаем teamId текущего пользователя
            if (teamId != -1) { // Проверяем, что пользователь состоит в команде
                ObservableList<String> projectNames = loadProjectsByTeamId(teamId); // Получаем список проектов
                dashboardProjectsList.getItems().addAll(projectNames); // Добавляем проекты в ListView
            } else {
                System.out.println("User is not part of any team.");
            }
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    // Обработчик смены активного меню
    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        User currentUser = Session.getInstance().getLoggedInUser();
        int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId());

        // Проверяем, является ли кнопка Tasks и есть ли доступ
        if (clickedButton == menuTasks) {
            if (teamId == -1) {
                return; // Не меняем стиль, если команда не найдена
            }
            ObservableList<String> projects = new DatabaseHandler().loadProjectsByTeamId(teamId);
            if (projects.isEmpty()) {
                return; // Не меняем стиль, если нет проектов
            }
        }

        // Проверяем, является ли кнопка Projects и есть ли доступ для не-админов
        if (clickedButton == menuProjects) {
            if (teamId == -1) {
                return; // Не меняем стиль, если команда не найдена
            }
            if (!"Admin".equalsIgnoreCase(currentUser.getUserRole())) {
                ObservableList<String> projects = new DatabaseHandler().loadProjectsByTeamId(teamId);
                if (projects.isEmpty()) {
                    return; // Не меняем стиль для не-админов, если нет проектов
                }
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

    private ObservableList<String> loadProjectsByTeamId(int teamId) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        String query = "SELECT " + Constant.PROJECT_NAME + " FROM " + Constant.PROJECT_TABLE +
                " WHERE " + Constant.PROJECT_TEAM_ID + " = ?";
        ObservableList<String> projectList = FXCollections.observableArrayList();

        try (Connection connection = dbHandler.getDbConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String projectName = rs.getString(Constant.PROJECT_NAME);
                projectList.add(projectName);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return projectList;
    }

    // Метод для загрузки задач по выбранному проекту
    private void loadTasksForSelectedProject(String projectName) {
        int projectId = new DatabaseHandler().getProjectIdByName(projectName);
        if (projectId != -1) {
            ObservableList<Task> tasks = new DatabaseHandler().loadTasksByProjectId(projectId);
            ObservableList<String> taskNames = FXCollections.observableArrayList();
            if (tasks.isEmpty()) {
                taskNames.add("No tasks available");
            } else {
                for (Task task : tasks) {
                    taskNames.add(task.getTitle());
                }
            }

            dashboardTasksList.setItems(taskNames);

            // Настраиваем CellFactory для цветовой индикации
            dashboardTasksList.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.equals("No tasks available")) {
                        setText(item);
                        setStyle("");
                    } else {
                        // Находим задачу по её заголовку
                        Task task = tasks.stream()
                                .filter(t -> t.getTitle().equals(item))
                                .findFirst()
                                .orElse(null);

                        if (task != null) {
                            setText(item);
                            if ("Completed".equalsIgnoreCase(task.getStatus())) {
                                setStyle("-fx-text-fill: #66cc66;");
                            } else if (task.isOverdue()) {
                                setStyle("-fx-text-fill: #ff6666;");
                            } else {
                                setStyle("-fx-text-fill: #b3b300;");
                            }
                        }
                    }
                }
            });
        } else {
            System.out.println("Selected project does not exist.");
        }
    }

    // Метод для установки обработчиков выбора
    private void setupSelectionHandlers() {
        // Устанавливаем обработчик только для dashboardProjectsList
        if (dashboardProjectsList != null && dashboardProjectsList.getSelectionModel() != null) {
            dashboardProjectsList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadTasksForSelectedProject(newSelection);
                }
            });
        }
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

    private Alert createAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.getDialogPane().setGraphic(null);

        return alert;
    }
}
