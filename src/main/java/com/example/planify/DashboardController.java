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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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

    @FXML
    void initialize() {
        User currentUser = Session.getInstance().getLoggedInUser();
        if (currentUser != null) {
            initializeUserData(currentUser);
        }

        setupUIElements();
        setupEventListeners();
        populateProjectLists(currentUser);
        setupUserAvatar();
        setupSelectionHandlers();

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

        menuProjects.setOnMouseClicked(event -> openNewWindow("projectsWindow.fxml"));
        menuTeam.setOnMouseClicked(event -> openNewWindow("teamWindow.fxml"));
        menuTasks.setOnMouseClicked(event -> openNewWindow("tasksWindow.fxml"));
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

    // Устанавливаем аватар пользователя
    private void setupUserAvatar() {
        Image image = new Image(getClass().getResourceAsStream("/com/example/planify/images/user.png"));
        circleBackAvatar.setRadius(50);
        circleBackAvatar.setFill(new ImagePattern(image));
        circleBackAvatar.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
        circleBackAvatar.setStrokeWidth(1);
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
                taskNames.add("No tasks available"); // Добавляем сообщение, если нет задач
            }
            else
            {
                for (Task task : tasks) {
                    taskNames.add(task.getTitle());
                }
            }

            dashboardTasksList.setItems(taskNames); // Обновляем ListView задач
        } else {
            System.out.println("Selected project does not exist.");
        }
    }

    private int getCreatedTaskActivity(User currentUser)
    {
        if (currentUser != null) {
            DatabaseHandler dbHandler = new DatabaseHandler();
            return dbHandler.getCreatedTaskCount(currentUser.getId());
        }
        return 0;
    }

    // Метод для установки обработчиков выбора
    private void setupSelectionHandlers() {
        // Устанавливаем обработчики выбора для каждого ListView
        dashboardProjectsList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                dashboardTasksList.getSelectionModel().clearSelection();
            }
        });

        dashboardTasksList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                dashboardProjectsList.getSelectionModel().clearSelection();
            }
        });
    }
}
