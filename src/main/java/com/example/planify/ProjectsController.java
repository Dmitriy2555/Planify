package com.example.planify;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

public class ProjectsController {

    @FXML
    private VBox projectsMenu;

    @FXML
    private Button menuDashboard, menuNotifications, menuProjects, menuSettings, menuTasks, menuTeam, newProjectButton, editProjectButton, deleteProjectButton;

    @FXML
    private Label dashboardEmailLabel, dashboardNameLabel, dashboardLabenSignOut, detailsNameOfProjectResultat, detailsStatusResultat, detailsDeadlineResultat;

    @FXML
    private ListView<String> projectsProjectsList, urgentTasksList;

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
            configureInterface(currentUser);
            avatarHandler = new AvatarHandler(circleBackAvatar, currentUser, false);
        }

        setupUIElements();
        setupEventListeners();
        populateProjectsList(currentUser);
        setupSelectionHandlers();

        // Обработчик выбора элемента в ListView
        // Добавляем обработчик событий для выбора проекта
        projectsProjectsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadTasksForSelectedProject(newValue);
                updateDetails(Session.getInstance().getLoggedInUser(), newValue);
            }
        });
    }

    // Устанавливаем данные текущего пользователя
    private void initializeUserData(User user) {
        dashboardNameLabel.setText(user.getFirstName());
        dashboardEmailLabel.setText(user.getUserEmail());
    }

    private void configureInterface(User user) {
        if ("Admin".equalsIgnoreCase(user.getUserRole())) {
            newProjectButton.setVisible(true);
            newProjectButton.setOnMouseClicked(event -> handleNewProject(user));
            deleteProjectButton.setOnAction(event -> handleRemoveProject());
            editProjectButton.setOnAction(event -> handleEditProject());
        }
        else
        {
            newProjectButton.setVisible(false);
        }
        editProjectButton.setVisible(false);
        deleteProjectButton.setVisible(false);
    }

    // Настроим элементы интерфейса
    private void setupUIElements() {
        menuProjects.getStyleClass().add("button-menu-active");
        activeButton = menuProjects;
    }

    // Добавляем обработчики событий
    private void setupEventListeners() {
        dashboardLabenSignOut.setOnMouseClicked(event -> handleSignOut());
        dashboardLabenSignOut.setOnMouseEntered(event -> dashboardLabenSignOut.setUnderline(true));
        dashboardLabenSignOut.setOnMouseExited(event -> dashboardLabenSignOut.setUnderline(false));

        menuDashboard.setOnMouseClicked(event -> openNewWindow("dashboardWindow.fxml"));
        menuTeam.setOnMouseClicked(event -> openNewWindow("teamWindow.fxml"));
        menuTasks.setOnMouseClicked(event -> openNewWindow("tasksWindow.fxml"));
        menuSettings.setOnMouseClicked(event -> openNewWindow("settingsWindow.fxml"));
    }

    // Заполняем список проектов
    private void populateProjectsList(User currentUser) {
        projectsProjectsList.getItems().clear(); // Очищаем текущий список

        if (currentUser != null) {
            int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId()); // Получаем teamId текущего пользователя
            if (teamId != -1) { // Проверяем, что пользователь состоит в команде
                ObservableList<String> projectNames = loadProjectsByTeamId(teamId); // Получаем список проектов
                projectsProjectsList.getItems().addAll(projectNames); // Добавляем проекты в ListView
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
    private void handleNewProject(User currentUser) {
            TextInputDialog dialog = createTextInputDialog("Create Project", "Enter the project's name to add:", "Name:");

            // Изменяем текст на кнопках
            ButtonType addButtonType = new ButtonType("Add", ButtonType.OK.getButtonData());
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
            dialog.getDialogPane().getButtonTypes().setAll(addButtonType, cancelButtonType);

            dialog.showAndWait().ifPresent(projectName -> {
                if (projectName.trim().isEmpty()) {
                    showAlertOneButton("Project name cannot be empty.");
                    return;
                }

                DatabaseHandler dbHandler = new DatabaseHandler();

                // Проверяем, существует ли уже проект с таким именем
                if (dbHandler.isProjectExists(projectName)) {
                    showAlertOneButton("A project with the name \"" + projectName + "\" already exists.");
                    return;
                }

                // Получаем teamId текущего пользователя
                int teamId = dbHandler.getTeamIdByUserId(currentUser.getId());
                System.out.println("TeamID: " + teamId);

                // Создаем новый проект
                boolean projectCreated = dbHandler.createProject(projectName, teamId);

                if (projectCreated) {
                    showSuccessAlert("Project \"" + projectName + "\" has been successfully created.");
                    // Обновляем интерфейс после создания проекта
                    populateProjectsList(Session.getInstance().getLoggedInUser());
                } else {
                    showAlertOneButton("Failed to create the project. Please try again.");
                }
            });
    }

    //удаление проекта
    private void handleRemoveProject() {
        String selectedProject = projectsProjectsList.getSelectionModel().getSelectedItem();

        if (showAlertTwoButton("Are you sure you want to remove " + selectedProject + "?")) {
                DatabaseHandler dbHandler = new DatabaseHandler();
                boolean isDeleted = dbHandler.deleteProjectByName(selectedProject);

                if (isDeleted) {
                    showSuccessAlert("Project \"" + selectedProject + "\" has been successfully deleted.");
                    System.out.println("Project \"" + selectedProject + "\" has been successfully deleted.");
                    // Обновляем список проектов
                    populateProjectsList(Session.getInstance().getLoggedInUser());
                    // Сбрасываем детали проекта
                    resetDetails();
                } else {
                    showAlertOneButton("Failed to delete the project. Please try again.");
                }

        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    //редактирование проекта
    private void handleEditProject() {
        String selectedProject = projectsProjectsList.getSelectionModel().getSelectedItem();

        TextInputDialog dialog = createTextInputDialog("Edit Project", "Select a new name and status:", "Name:");

        // Изменяем текст на кнопках
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, cancelButtonType);

        // ChoiceBox для статуса
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
        statusChoiceBox.getItems().addAll("Planned", "In Progress", "Completed");
        DatabaseHandler dbHandler = new DatabaseHandler();
        statusChoiceBox.setValue(dbHandler.getProjectStatus(selectedProject));

        // Текстовое поле (из TextInputDialog)
        TextField nameField = dialog.getEditor();
        nameField.setText(selectedProject); // заполняем текущим именем

        // Собираем кастомный layout
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("New name:"), nameField,
                new Label("New status:"), statusChoiceBox
        );

        // Заменяем стандартное содержимое диалога
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(projectName -> {
            if (projectName.trim().isEmpty()) {
                showAlertOneButton("Project name cannot be empty.");
                return;
            }

            // Проверяем, существует ли уже проект с таким именем
            if (!projectName.equals(selectedProject) && dbHandler.isProjectExists(projectName)) {
                showAlertOneButton("A project with the name \"" + projectName + "\" already exists.");
                return;
            }

            String selectedStatus = statusChoiceBox.getValue();

            // Метод обновления проекта с новым именем и статусом
            dbHandler.updateProject(selectedProject, projectName, selectedStatus);
            showSuccessAlert("Project \"" + selectedProject + "\" has been successfully changed.");

            System.out.println("Project \"" + selectedProject + "\" has been successfully changed.");
            // Обновляем список проектов
            populateProjectsList(Session.getInstance().getLoggedInUser());
            // Сбрасываем детали проекта
            resetDetails();
        });
    }

    //диалоговое окно
    private TextInputDialog createTextInputDialog(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        dialog.getDialogPane().setGraphic(null);

        // Устанавливаем иконку для окна диалога
        Image icon = new Image(getClass().getResourceAsStream("/com/example/planify/images/iconCreate.png"));
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

    private void updateDetails(User currentUser, String projectName) {
        // Получаем выбранный проект напрямую из ListView
        String selectedProject = projectsProjectsList.getSelectionModel().getSelectedItem();

        // Проверяем, выбран ли проект
        boolean isProjectSelected = selectedProject != null;

        if ("Admin".equalsIgnoreCase(currentUser.getUserRole()) && isProjectSelected) {
            editProjectButton.setVisible(true);
            deleteProjectButton.setVisible(true);
        }
        else
        {
            editProjectButton.setVisible(false);
            deleteProjectButton.setVisible(false);
        }
        // Обновляем детали проекта только если проект выбран
        if (isProjectSelected) {
            detailsNameOfProjectResultat.setText(selectedProject);
            DatabaseHandler dbHandler = new DatabaseHandler();
            detailsStatusResultat.setText(dbHandler.getProjectStatus(selectedProject));
            detailsDeadlineResultat.setText(String.valueOf(getCreatedTaskFromProject()));
        } else {
            // Очищаем поля, если проект не выбран
            detailsNameOfProjectResultat.setText("");
            detailsStatusResultat.setText("");
            detailsDeadlineResultat.setText("");
        }
    }

    private void resetDetails() {
        detailsNameOfProjectResultat.setText("Name: ");
        detailsStatusResultat.setText("Status: ");
        editProjectButton.setVisible(false);
        deleteProjectButton.setVisible(false);
    }

    // Метод для загрузки задач по выбранному проекту
    private void loadTasksForSelectedProject(String projectName) {
        int projectId = new DatabaseHandler().getProjectIdByName(projectName);
        if (projectId != -1) {
            ObservableList<Task> tasks = new DatabaseHandler().loadTasksByProjectId(projectId);
            ObservableList<String> taskNames = FXCollections.observableArrayList();

            boolean hasTasks = false;
            for(Task task: tasks)
            {
                if("To Do".equalsIgnoreCase(task.getStatus()) || "In Progress".equalsIgnoreCase(task.getStatus()))
                {
                    taskNames.add(task.getTitle());
                    hasTasks = true;

                }
            }

            if (!hasTasks) {
                taskNames.add("No tasks available"); // Добавляем сообщение, если нет задач с нужным статусом
            }

            urgentTasksList.setItems(taskNames); // Обновляем ListView задач
        } else {
            System.out.println("Selected project does not exist.");
        }
    }

    private int getCreatedTaskFromProject()
    {
        String selectedProject = projectsProjectsList.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            DatabaseHandler dbHandler = new DatabaseHandler();
            int projectId = dbHandler.getProjectIdByName(selectedProject);
            if (projectId != -1) { // Проверяем, что проект найден
                return dbHandler.getCreatedTaskByProjectId(projectId);
            }
        }
        return 0;
    }

    // Метод для установки обработчиков выбора
    private void setupSelectionHandlers() {
        // Устанавливаем обработчики выбора для каждого ListView
        projectsProjectsList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                urgentTasksList.getSelectionModel().clearSelection();
            }

            editProjectButton.setVisible(true);
            deleteProjectButton.setVisible(true);
        });

        urgentTasksList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                projectsProjectsList.getSelectionModel().clearSelection();
            }

            editProjectButton.setVisible(false);
            deleteProjectButton.setVisible(false);
        });
    }
}
