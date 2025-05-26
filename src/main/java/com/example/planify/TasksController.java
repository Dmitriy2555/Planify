package com.example.planify;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TasksController {

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
    private Button menuDashboard, activeButton, ButtonCreateTask, ButtonDeleteTask;

    @FXML
    private Button menuProjects, ButtonEditTask;

    @FXML
    private Button menuSettings;

    @FXML
    private Button menuTasks;

    @FXML
    private Button menuTeam;

    @FXML
    private VBox vboxAvatarNameEmail;

    @FXML
    private VBox vboxMenu;

    @FXML
    private VBox vboxSignOut;

    @FXML
    private VBox tasksToDoVbox, tasksToDoVboxBack;

    @FXML
    private VBox tasksInProgressVbox, tasksInProgressVboxBack;

    @FXML
    private VBox tasksCompletedVbox, tasksCompletedVboxBack;

    @FXML
    private ChoiceBox tasksChoiceBoxProject;

    @FXML
    private ListView tasksToDoListViewBack, tasksInProgressListViewBack, tasksCompletedListViewBack;

    @FXML
    private CheckBox checkBoxOnlyMyTasks;

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
        setupSelectionHandlers();


        // Получаем teamId пользователя
        int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId());

        // Проверяем, что пользователь состоит в команде
        if (teamId != -1) {
            populateProjectChoiceBox(currentUser);

            // Загружаем последний открытый проект только если пользователь в команде
            String lastOpenedProject = Session.getInstance().getLastOpenedProject();
            if (lastOpenedProject != null) {
                // Дополнительно проверяем, что проект принадлежит команде пользователя
                if (isProjectBelongsToTeam(lastOpenedProject, teamId)) {
                    tasksChoiceBoxProject.setValue(lastOpenedProject);
                    loadTasksForProject(lastOpenedProject);
                } else {
                    // Очищаем последний открытый проект, если он не принадлежит текущей команде
                    Session.getInstance().setLastOpenedProject(null);
                }
            }

            handleProjectSelection();
        } else {
            // Пользователь не в команде - очищаем список проектов и отключаем элементы управления
            tasksChoiceBoxProject.getItems().clear();
            tasksChoiceBoxProject.setDisable(true);
            ButtonCreateTask.setDisable(true);
            ButtonEditTask.setDisable(true);
            ButtonDeleteTask.setDisable(true);

            // Показываем сообщение пользователю
            showAlertOneButton("You are not a member of any team. Join a team to work on projects.");
        }

        // Устанавливаем начальную видимость кнопок
        updateButtonVisibilityBasedOnSelection();

        // Настраиваем слушатель для CheckBox
        checkBoxOnlyMyTasks.setOnAction(event -> {
            String selectedProject = (String) tasksChoiceBoxProject.getValue();
            if (selectedProject != null) {
                loadTasksForProject(selectedProject); // Перезагружаем задачи с учётом фильтра
            }
        });
    }

    // Метод для проверки принадлежности проекта к команде
    private boolean isProjectBelongsToTeam(String projectName, int teamId) {
        DatabaseHandler dbHandler = new DatabaseHandler();
        int projectId = dbHandler.getProjectIdByName(projectName);
        if (projectId != -1) {
            int projectTeamId = dbHandler.getTeamIdByProjectId(projectId);
            return projectTeamId == teamId;
        }
        return false;
    }

    // Устанавливаем данные текущего пользователя
    private void initializeUserData(User user) {
        dashboardNameLabel.setText(user.getFirstName());
        dashboardEmailLabel.setText(user.getUserEmail());
    }

    private void configureInterface(User user) {
        ButtonCreateTask.setVisible(true);
        ButtonEditTask.setVisible(true);
        ButtonDeleteTask.setVisible(false);
        if ("Admin".equalsIgnoreCase(user.getUserRole())) {
            ButtonDeleteTask.setVisible(true);
        }
    }

    // Настроим элементы интерфейса
    private void setupUIElements() {
        menuTasks.getStyleClass().add("button-menu-active");
        activeButton = menuTasks;
    }

    // Добавляем обработчики событий
    private void setupEventListeners() {
        dashboardLabenSignOut.setOnMouseClicked(event -> handleSignOut());
        dashboardLabenSignOut.setOnMouseEntered(event -> dashboardLabenSignOut.setUnderline(true));
        dashboardLabenSignOut.setOnMouseExited(event -> dashboardLabenSignOut.setUnderline(false));

        menuDashboard.setOnMouseClicked(event -> openNewWindow("dashboardWindow.fxml"));
        menuTeam.setOnMouseClicked(event -> openNewWindow("teamWindow.fxml"));
        menuProjects.setOnMouseClicked(event -> openNewWindow("projectsWindow.fxml"));
        menuSettings.setOnMouseClicked(event -> openNewWindow("settingsWindow.fxml"));

        ButtonCreateTask.setOnMouseClicked(event -> handleCreateTask());
        ButtonDeleteTask.setOnMouseClicked(event -> handleRemoveTask());
        ButtonEditTask.setOnMouseClicked(event -> handleEditTask());
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

    private void handleCreateTask() {
        TextInputDialog dialog = createTextInputDialog("Create Task", "Enter task details:", "Name:");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(createButtonType, cancelButtonType);

        TextField nameField = dialog.getEditor();
        nameField.setPromptText("Task Name");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Deadline");

        ChoiceBox<String> assigneeChoiceBox = new ChoiceBox<>();
        Label assigneeLabel = new Label("Assigned to:");

        User currentUser = Session.getInstance().getLoggedInUser();
        int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId());

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Deadline:"), deadlinePicker
        );

        if ("Admin".equalsIgnoreCase(currentUser.getUserRole())) {
            ObservableList<User> teamMembers = new DatabaseHandler().loadTeamMembersByTeamId(teamId);
            ObservableList<String> assigneeNames = FXCollections.observableArrayList();

            for (User member : teamMembers) {
                assigneeNames.add(member.getFirstName() + " " + member.getLastName());
            }

            assigneeChoiceBox.getItems().addAll(assigneeNames);
            content.getChildren().addAll(assigneeLabel, assigneeChoiceBox); // Добавляем поля для админа
        } else {
            assigneeChoiceBox.setDisable(true);
        }

        // Заменяем стандартное содержимое диалога
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String taskName = nameField.getText().trim();
                String selectedProject = tasksChoiceBoxProject.getValue().toString(); // Берем текущий проект с формы
                LocalDate deadline = deadlinePicker.getValue();
                String assigneeName = "Admin".equalsIgnoreCase(currentUser.getUserRole()) ? assigneeChoiceBox.getValue() : currentUser.getFirstName() + " " + currentUser.getLastName();
                String assigneeFirstName = currentUser.getFirstName();

                if (taskName.isEmpty() || selectedProject == null || deadline == null) {
                    showAlertOneButton("All fields are required.");
                    return null;
                }

                // Проверка, что дедлайн не в прошлом
                if (deadline.isBefore(LocalDate.now())) {
                    showAlertOneButton("Deadline cannot be in the past.");
                    return null;
                }

                // Получаем ID проекта
                int projectId = new DatabaseHandler().getProjectIdByName(selectedProject);
                if (projectId == -1) {
                    showAlertOneButton("Selected project does not exist.");
                    return null;
                }

                // Получаем ID исполнителя
                int assigneeId = new DatabaseHandler().getUserIdByName(assigneeName);
                if (assigneeId == -1) {
                    showAlertOneButton("Selected assignee does not exist.");
                    return null;
                }

                // Создаем задачу в базе данных
                boolean taskCreated = new DatabaseHandler().createTask(taskName, projectId, deadline, assigneeId, currentUser.getId());
                if (taskCreated) {
                    showSuccessAlert("Task created successfully.");
                    VBox taskBlock = createTaskBlock(taskName, deadline, assigneeName);
                    addTaskBlockToColumn(taskBlock, "To Do"); // Добавляем в VBox "To Do"

                    // Отправка сообщения пользователю на почту
                    EmailSender emailSender = new EmailSender();
                    String emailAssignee = new DatabaseHandler().getUserEmailById(assigneeId);
                    emailSender.EmailSend(emailAssignee, assigneeName, "You have a new task!\nTo do: " + taskName + "\nDeadline: " + deadline);
                } else {
                    showAlertOneButton("Failed to create task.");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    //удаление проекта
    private void handleRemoveTask() {
        // Проверяем, выбран ли проект
        String selectedProject = (String) tasksChoiceBoxProject.getValue();
        if (selectedProject == null) {
            showAlertOneButton("Please select a project.");
            return;
        }

        // Проверяем, выбран ли элемент в одном из ListView
        VBox selectedTaskBox = null;
        if (tasksToDoListViewBack.getSelectionModel().getSelectedItem() != null) {
            selectedTaskBox = (VBox) tasksToDoListViewBack.getSelectionModel().getSelectedItem();
        } else if (tasksInProgressListViewBack.getSelectionModel().getSelectedItem() != null) {
            selectedTaskBox = (VBox) tasksInProgressListViewBack.getSelectionModel().getSelectedItem();
        } else if (tasksCompletedListViewBack.getSelectionModel().getSelectedItem() != null) {
            selectedTaskBox = (VBox) tasksCompletedListViewBack.getSelectionModel().getSelectedItem();
        }

        if (selectedTaskBox == null) {
            showAlertOneButton("Please select a task to delete.");
            return;
        }

        // Извлекаем имя задачи из пользовательских данных
        String selectedTask = (String) selectedTaskBox.getUserData();
        System.out.println(selectedTask);

        if (selectedTask != null) {
            // Выводим диалоговое окно с подтверждением удаления
            if (showAlertTwoButton("Are you sure you want to remove the task \"" + selectedTask + "\"?")) {
                // Удаляем задачу из базы данных
                DatabaseHandler dbHandler = new DatabaseHandler();
                boolean isDeleted = dbHandler.deleteTaskByName(selectedTask);

                if (isDeleted) {
                    showSuccessAlert("Task \"" + selectedTask + "\" has been successfully deleted.");
                    // Обновляем списки задач
                    clearTaskColumns();
                    loadTasksForProject(selectedProject);
                } else {
                    showAlertOneButton("Failed to delete the task. Please try again.");
                }
            }
        } else {
            showAlertOneButton("Failed to retrieve task name.");
        }
    }

    //редактирование задачи
    private void handleEditTask() {
        // Проверяем, выбран ли проект
        String selectedProject = (String) tasksChoiceBoxProject.getValue();
        if (selectedProject == null) {
            showAlertOneButton("Please select a project.");
            return;
        }

        // Проверяем, выбран ли элемент в одном из ListView
        VBox selectedTaskBox = null;
        if (tasksToDoListViewBack.getSelectionModel().getSelectedItem() != null) {
            selectedTaskBox = (VBox) tasksToDoListViewBack.getSelectionModel().getSelectedItem();
        } else if (tasksInProgressListViewBack.getSelectionModel().getSelectedItem() != null) {
            selectedTaskBox = (VBox) tasksInProgressListViewBack.getSelectionModel().getSelectedItem();
        } else if (tasksCompletedListViewBack.getSelectionModel().getSelectedItem() != null) {
            selectedTaskBox = (VBox) tasksCompletedListViewBack.getSelectionModel().getSelectedItem();
        }

        if (selectedTaskBox == null) {
            showAlertOneButton("Please select a task to edit.");
            return;
        }

        // Извлекаем имя задачи из пользовательских данных
        String selectedTaskName = (String) selectedTaskBox.getUserData();
        if (selectedTaskName == null) {
            showAlertOneButton("Failed to retrieve task name.");
            return;
        }

        DatabaseHandler dbHandler = new DatabaseHandler();
        Task selectedTask = dbHandler.getTaskByName(selectedTaskName);
        if (selectedTask == null) {
            showAlertOneButton("Task not found in the database.");
            return;
        }

        User currentUser = Session.getInstance().getLoggedInUser();
        boolean isAdmin = "Admin".equalsIgnoreCase(currentUser.getUserRole());
        boolean isTaskOwner = selectedTask.getCreatedBy().equals(String.valueOf(currentUser.getId()));
        boolean isTaskAssignee = selectedTask.getAssignedTo().equals(String.valueOf(currentUser.getId()));

        if (!isAdmin && !isTaskOwner && !isTaskAssignee) {
            showAlertOneButton("You do not have permission to edit this task.");
            return;
        }

        TextInputDialog dialog = createTextInputDialog("Edit Task", "Edit task details:", "Name:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(saveButtonType, cancelButtonType);

        TextField nameField = dialog.getEditor();
        nameField.setText(selectedTask.getTitle());

        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>();
        statusChoiceBox.getItems().addAll("To Do", "In Progress", "Completed");
        statusChoiceBox.setValue(selectedTask.getStatus());

        VBox content;
        if (isAdmin) {
            DatePicker deadlinePicker = new DatePicker(selectedTask.getDeadline());

            ChoiceBox<String> assigneeChoiceBox = new ChoiceBox<>();
            ObservableList<User> teamMembers = dbHandler.loadTeamMembersByTeamId(dbHandler.getTeamIdByUserId(currentUser.getId()));
            ObservableList<String> assigneeNames = FXCollections.observableArrayList();

            for (User member : teamMembers) {
                assigneeNames.add(member.getFirstName() + " " + member.getLastName());
            }

            assigneeChoiceBox.getItems().addAll(assigneeNames);
            assigneeChoiceBox.setValue(dbHandler.getUserNameById(Integer.parseInt(selectedTask.getAssignedTo())));

            content = new VBox(10);
            content.setPadding(new Insets(10));
            content.getChildren().addAll(
                    new Label("New name:"), nameField,
                    new Label("New status:"), statusChoiceBox,
                    new Label("New deadline:"), deadlinePicker,
                    new Label("New assignee:"), assigneeChoiceBox
            );
            dialog.getDialogPane().setContent(content);

            dialog.showAndWait().ifPresent(taskName -> {
                if (taskName.trim().isEmpty()) {
                    showAlertOneButton("Task name cannot be empty.");
                    return;
                }

                String selectedStatus = statusChoiceBox.getValue();
                LocalDate selectedDeadline = deadlinePicker.getValue();
                String selectedAssignee = assigneeChoiceBox.getValue();
                int assigneeId = dbHandler.getUserIdByName(selectedAssignee);

                dbHandler.updateTask(selectedTask.getId(), taskName, selectedStatus, selectedDeadline, assigneeId);
                showSuccessAlert("Task \"" + selectedTask.getTitle() + "\" successfully updated.");
                clearTaskColumns();
                loadTasksForProject(selectedProject);
            });
        } else {
            content = new VBox(10);
            content.setPadding(new Insets(10));
            content.getChildren().addAll(
                    new Label("New name:"), nameField,
                    new Label("New status:"), statusChoiceBox
            );

            dialog.getDialogPane().setContent(content);

            dialog.showAndWait().ifPresent(taskName -> {
                if (taskName.trim().isEmpty()) {
                    showAlertOneButton("Task name cannot be empty.");
                    return;
                }

                String selectedStatus = statusChoiceBox.getValue();

                // Для не админов сохраняем оригинальный дедлайн и исполнителя
                dbHandler.updateTask(selectedTask.getId(), taskName, selectedStatus, selectedTask.getDeadline(), Integer.parseInt(selectedTask.getAssignedTo()));
                showSuccessAlert("Task \"" + selectedTask.getTitle() + "\" successfully updated.");
                clearTaskColumns();
                loadTasksForProject(selectedProject);
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

    // Метод для заполнения ChoiceBox проектами
    private void populateProjectChoiceBox(User currentUser) {
        tasksChoiceBoxProject.getItems().clear(); // Очищаем текущий список

        if (currentUser != null) {
            int teamId = new DatabaseHandler().getTeamIdByUserId(currentUser.getId()); // Получаем teamId текущего пользователя
            if (teamId != -1) { // Проверяем, что пользователь состоит в команде
                DatabaseHandler dbHandler = new DatabaseHandler();
                ObservableList<String> projectNames = dbHandler.loadProjectsByTeamId(teamId); // Получаем список проектов
                tasksChoiceBoxProject.getItems().addAll(projectNames); // Добавляем проекты в ChoiceBox
            } else {
                System.out.println("User is not part of any team.");
            }
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    private VBox createTaskBlock(String taskName, LocalDate deadline, String assigneeName) {
        VBox taskBlock = new VBox();
        taskBlock.setPadding(new Insets(5)); // Уменьшаем отступы
        taskBlock.setPrefWidth(120); // Уменьшаем ширину
        taskBlock.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-border-style: solid; -fx-background-radius: 10; -fx-border-radius: 10; -fx-background-color: #f9f9f9;");

        // Добавляем пользовательские данные
        taskBlock.setUserData(taskName);

        // Название задачи
        Text taskNameText = new Text(taskName);
        taskNameText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-alignment: center;");
        taskNameText.setWrappingWidth(119); // Ограничиваем ширину текста для переноса

        // Проверяем, является ли assigneeName числом (ID)
        int idAssignee;
        try {
            idAssignee = Integer.parseInt(assigneeName); // Пытаемся преобразовать в число
            assigneeName = new DatabaseHandler().getUserNameById(idAssignee); // Получаем имя по ID
        } catch (NumberFormatException e) {
            // Если преобразование не удалось, предполагаем, что assigneeName уже является именем
            idAssignee = -1; // Устанавливаем недопустимый ID, так как имя уже известно
        }

        // Исполнитель и дедлайн
        VBox detailsBox = new VBox(5); // Устанавливаем отступ между элементами в 5 пикселей
        Label assigneeLabel = new Label("Assigned to: " + assigneeName);
        Label deadlineLabel = new Label("Deadline: " + deadline.toString());

        // Проверяем, просрочена ли задача
        boolean isOverdue = deadline.isBefore(LocalDate.now());

        // Устанавливаем стиль текста в зависимости от просрочки
        if (isOverdue) {
            taskNameText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-alignment: center; -fx-fill: #ff6666;");
            assigneeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
            deadlineLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
        } else {
            taskNameText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-alignment: center; -fx-fill: black;");
            assigneeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
            deadlineLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
        }

        detailsBox.getChildren().addAll(assigneeLabel, deadlineLabel);
        detailsBox.setAlignment(Pos.CENTER);

        // Устанавливаем отступ между заголовком и остальными элементами
        VBox contentBox = new VBox(5); // Устанавливаем отступ между элементами в 5 пикселей
        contentBox.getChildren().addAll(taskNameText, detailsBox);
        contentBox.setAlignment(Pos.CENTER);

        taskBlock.getChildren().add(contentBox);
        taskBlock.setAlignment(Pos.CENTER); // Центрирование содержимого

        return taskBlock;
    }

    private void addTaskBlockToColumn(VBox taskBlock, String status) {
        if ("To Do".equalsIgnoreCase(status)) {
            tasksToDoListViewBack.getItems().add(taskBlock);
        } else if ("In Progress".equalsIgnoreCase(status)) {
            tasksInProgressListViewBack.getItems().add(taskBlock);
        } else if ("Completed".equalsIgnoreCase(status)) {
            tasksCompletedListViewBack.getItems().add(taskBlock);
        }
    }

    // Обработчик выбора проекта
    private void handleProjectSelection() {
        tasksChoiceBoxProject.setOnAction(event -> {
            Object selectedProjectObj = tasksChoiceBoxProject.getValue();

            if (selectedProjectObj != null) {
                String selectedProject = (String) selectedProjectObj; // Преобразование Object к String
                clearTaskColumns(); // Очищаем все колонки перед загрузкой новых задач
                loadTasksForProject(selectedProject);

                // Сохраняем выбранный проект в сессии
                Session.getInstance().setLastOpenedProject(selectedProject);

                // Показываем только кнопку "Create Task", скрываем остальные
                ButtonCreateTask.setVisible(true);
                ButtonEditTask.setVisible(false);
                ButtonDeleteTask.setVisible(false);
            } else {
                // Если проект не выбран, скрываем все кнопки
                ButtonCreateTask.setVisible(false);
                ButtonEditTask.setVisible(false);
                ButtonDeleteTask.setVisible(false);
            }
        });
    }

    private void loadTasksForProject(String projectName) {
        int projectId = new DatabaseHandler().getProjectIdByName(projectName);
        if (projectId != -1) {
            ObservableList<Task> tasks = new DatabaseHandler().loadTasksByProjectId(projectId);
            User currentUser = Session.getInstance().getLoggedInUser();
            int userId = currentUser.getId();

            clearTaskColumns(); // Очищаем колонки перед загрузкой

            for (Task task : tasks) {
                // Преобразуем assignedTo (строка с userId) в int
                int assigneeId;
                try {
                    assigneeId = Integer.parseInt(task.getAssignedTo());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid assignee ID format for task: " + task.getTitle() + ", assignee: " + task.getAssignedTo());
                    continue; // Пропускаем задачу с некорректным ID
                }

                // Проверяем, нужно ли фильтровать по текущему пользователю
                if (!checkBoxOnlyMyTasks.isSelected() || assigneeId == userId) {
                    // Получаем имя исполнителя для отображения
                    String assigneeName = task.getAssignedTo(); // Используем как временное решение, но лучше получить имя через DB
                    DatabaseHandler dbHandler = new DatabaseHandler();
                    assigneeName = dbHandler.getUserNameById(assigneeId); // Получаем имя по userId
                    if (assigneeName == null) assigneeName = "Unknown"; // Если имя не найдено

                    VBox taskBlock = createTaskBlock(task.getTitle(), task.getDeadline(), assigneeName);
                    addTaskBlockToColumn(taskBlock, task.getStatus());
                }
            }
        } else {
            showAlertOneButton("Selected project does not exist.");
        }
    }

    // Метод для очистки всех колонок задач
    private void clearTaskColumns() {
        tasksToDoListViewBack.getItems().clear();
        tasksInProgressListViewBack.getItems().clear();
        tasksCompletedListViewBack.getItems().clear();
    }

    // Метод для установки обработчиков выбора
    private void setupSelectionHandlers() {
        // Устанавливаем обработчики выбора для каждого ListView
        tasksToDoListViewBack.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tasksInProgressListViewBack.getSelectionModel().clearSelection();
                tasksCompletedListViewBack.getSelectionModel().clearSelection();

                // Если задача выбрана и проект выбран, показываем кнопки "Edit" и "Delete"
                if (tasksChoiceBoxProject.getValue() != null) {
                    ButtonCreateTask.setVisible(true); // "Create Task" остаётся видимой
                    ButtonEditTask.setVisible(true);
                    ButtonDeleteTask.setVisible("Admin".equalsIgnoreCase(Session.getInstance().getLoggedInUser().getUserRole()));
                }
            } else {
                updateButtonVisibilityBasedOnSelection();
            }
        });

        tasksInProgressListViewBack.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tasksToDoListViewBack.getSelectionModel().clearSelection();
                tasksCompletedListViewBack.getSelectionModel().clearSelection();

                // Если задача выбрана и проект выбран, показываем кнопки "Edit" и "Delete"
                if (tasksChoiceBoxProject.getValue() != null) {
                    ButtonCreateTask.setVisible(true); // "Create Task" остаётся видимой
                    ButtonEditTask.setVisible(true);
                    ButtonDeleteTask.setVisible("Admin".equalsIgnoreCase(Session.getInstance().getLoggedInUser().getUserRole()));
                }
            } else {
                updateButtonVisibilityBasedOnSelection();
            }
        });

        tasksCompletedListViewBack.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tasksToDoListViewBack.getSelectionModel().clearSelection();
                tasksInProgressListViewBack.getSelectionModel().clearSelection();

                // Если задача выбрана и проект выбран, показываем кнопки "Edit" и "Delete"
                if (tasksChoiceBoxProject.getValue() != null) {
                    ButtonCreateTask.setVisible(true); // "Create Task" остаётся видимой
                    ButtonEditTask.setVisible(true);
                    ButtonDeleteTask.setVisible("Admin".equalsIgnoreCase(Session.getInstance().getLoggedInUser().getUserRole()));
                }
            } else {
                updateButtonVisibilityBasedOnSelection();
            }
        });
    }

    private void updateButtonVisibilityBasedOnSelection() {
        boolean isTaskSelected = tasksToDoListViewBack.getSelectionModel().getSelectedItem() != null ||
                tasksInProgressListViewBack.getSelectionModel().getSelectedItem() != null ||
                tasksCompletedListViewBack.getSelectionModel().getSelectedItem() != null;

        if (tasksChoiceBoxProject.getValue() != null) {
            ButtonCreateTask.setVisible(true); // "Create Task" видима, если проект выбран
            ButtonEditTask.setVisible(isTaskSelected); // "Edit Task" видима только если выбрана задача
            ButtonDeleteTask.setVisible(isTaskSelected && "Admin".equalsIgnoreCase(Session.getInstance().getLoggedInUser().getUserRole())); // "Delete Task" видима только для админа и если выбрана задача
        } else {
            // Если проект не выбран, скрываем все кнопки
            ButtonCreateTask.setVisible(false);
            ButtonEditTask.setVisible(false);
            ButtonDeleteTask.setVisible(false);
        }
    }
}
