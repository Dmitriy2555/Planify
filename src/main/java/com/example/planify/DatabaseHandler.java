package com.example.planify;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends Configuration{
    Connection dbConnection;

    public Connection getDbConnection()
            throws ClassNotFoundException, SQLException
            {
                String connectionString = "jdbc:mysql://"
                        + dbHost + ":"
                            + dbPort + "/" + dbName;

                Class.forName("com.mysql.jdbc.Driver");

                dbConnection = DriverManager.getConnection(connectionString,
                        dbUser, dbPass);

                return dbConnection;
            }

            public void signUpUser(User user) {
                String insert = "INSERT INTO " + Constant.USER_TABLE + "(" +
                        Constant.USER_FIRSTNAME + "," + Constant.USER_LASTNAME + "," +
                        Constant.USER_EMAIL + "," + Constant.USER_PASS + "," +
                        Constant.USER_GENDER + "," + Constant.USER_ROLE + ")" +
                        "VALUES(?,?,?,?,?,?)";

                try (Connection connection = getDbConnection();
                     PreparedStatement prSt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {

                    prSt.setString(1, user.getFirstName());
                    prSt.setString(2, user.getLastName());
                    prSt.setString(3, user.getUserEmail());
                    prSt.setString(4, user.getUserPassword());
                    prSt.setString(5, user.getUserGender());
                    prSt.setString(6, user.getUserRole());

                    int affectedRows = prSt.executeUpdate();

                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = prSt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int generatedId = generatedKeys.getInt(1);
                                user.setId(generatedId);
                                System.out.println("User created with ID: " + generatedId);
                            }
                        }
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            public ResultSet getUser(User user)
            {
                ResultSet resSet = null;

                String select = "SELECT * FROM " + Constant.USER_TABLE + " WHERE " +
                        Constant.USER_EMAIL + "=? AND " + Constant.USER_PASS + "=?";

                try {
                    PreparedStatement prSt = getDbConnection().prepareStatement(select);
                    prSt.setString(1, user.getUserEmail());
                    prSt.setString(2, user.getUserPassword());

                    resSet = prSt.executeQuery();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                return resSet;
            }

            public User getUserByFullName(String firstName, String lastName) {
                String selectQuery = "SELECT * FROM users WHERE firstname = ? AND lastname = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {

                    pstmt.setString(1, firstName);
                    pstmt.setString(2, lastName);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setFirstName(rs.getString("firstname"));
                        user.setLastName(rs.getString("lastname"));
                        user.setUserEmail(rs.getString("email"));
                        user.setUserPassword(rs.getString("password"));
                        user.setUserGender(rs.getString("gender"));
                        user.setUserRole(rs.getString("role"));
                        return user;
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }

            public String getUserNameById(int userId) {
                String query = "SELECT " + Constant.USER_FIRSTNAME + ", " + Constant.USER_LASTNAME +
                        " FROM " + Constant.USER_TABLE + " WHERE " + Constant.USER_ID + " = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        String firstName = rs.getString(Constant.USER_FIRSTNAME);
                        String lastName = rs.getString(Constant.USER_LASTNAME);
                        return firstName + " " + lastName;
                    } else {
                        return null; // Возвращаем null, если пользователь не найден
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            //проверка, существует ли уже созданная почта в базе данных
            public boolean isEmailExists(String email) {
                String query = "SELECT COUNT(1) FROM users WHERE email = ?";
                try (PreparedStatement pstmt = getDbConnection().prepareStatement(query)) {
                    pstmt.setString(1, email);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }

            //проверяет, существует ли команда с данным именем
            public boolean isTeamExists(String teamName) {
                String query = "SELECT COUNT(*) FROM " + Constant.TEAM_TABLE + " WHERE " + Constant.TEAM_NAME + " = ?";
                try (PreparedStatement pstmt = getDbConnection().prepareStatement(query)) {
                    pstmt.setString(1, teamName);
                    ResultSet rs = pstmt.executeQuery();
                    return rs.next() && rs.getInt(1) > 0;
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            // Получаем пользователя по Email
            public User getUserByEmail(String email) {
                String query = "SELECT * FROM users WHERE email = ?";
                try (PreparedStatement pstmt = getDbConnection().prepareStatement(query)) {
                    pstmt.setString(1, email);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        int id = rs.getInt("id"); // Получаем ID пользователя
                        System.out.println("Loaded User ID: " + id); // Отладочный вывод

                        String firstName = rs.getString("firstname");
                        String lastName = rs.getString("lastname");
                        String userEmail = rs.getString("email");
                        String userPassword = rs.getString("password"); // Обычно не нужно, но пусть будет
                        String userGender = rs.getString("gender");
                        String userRole = rs.getString("role");

                        User user = new User(firstName, lastName, userEmail, userPassword, userGender, userRole);
                        user.setId(id); // Устанавливаем ID пользователя
                        return user;
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null; // Не найден
            }

            public int getUserIdByName(String fullName) {
                String query = "SELECT " + Constant.USER_ID + " FROM " + Constant.USER_TABLE +
                        " WHERE CONCAT(" + Constant.USER_FIRSTNAME + ", ' ', " + Constant.USER_LASTNAME + ") = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, fullName);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(Constant.USER_ID);
                    } else {
                        return -1; // Возвращаем -1, если пользователь не найден
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return -1;
                }
            }

            public int getTeamIdByName(String teamName) {
                String query = "SELECT " + Constant.TEAM_ID + " FROM " + Constant.TEAM_TABLE + " WHERE " + Constant.TEAM_NAME + " = ?";
                try (PreparedStatement pstmt = getDbConnection().prepareStatement(query)) {
                    pstmt.setString(1, teamName);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        return rs.getInt(Constant.TEAM_ID);
                    } else {
                        throw new RuntimeException("Team not found.");
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error retrieving team ID.");
                }
            }

            public int getTeamIdByUserId(int userId) {
                String query = "SELECT " + Constant.TEAM_MEMBERS_TEAM_ID + " FROM " + Constant.TEAM_MEMBERS_TABLE +
                        " WHERE " + Constant.TEAM_MEMBERS_USER_ID + " = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        int teamId = rs.getInt(Constant.TEAM_MEMBERS_TEAM_ID);
                        System.out.println("Retrieved Team ID for user " + userId + ": " + teamId); // Логирование
                        return teamId;
                    } else {
                         return -1;
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return -1;
                }
            }


            // Метод для создания команды (добавляет запись в таблицу teams)
            public void createTeam(String teamName, int adminId) {
                String insert = "INSERT INTO " + Constant.TEAM_TABLE + " (" +
                        Constant.TEAM_NAME + ", " + Constant.TEAM_ADMIN_ID + ") VALUES (?, ?)";

                try (PreparedStatement prSt = getDbConnection().prepareStatement(insert)) {
                    prSt.setString(1, teamName); // Устанавливаем название команды
                    prSt.setInt(2, adminId);     // Устанавливаем ID администратора
                    prSt.executeUpdate();        // Выполняем SQL-запрос
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace(); // Вывод ошибки в случае неудачи
                }
            }

            // Метод для добавления пользователя в команду (запись в таблицу team_members)
            public void addUserToTeam(int teamId, int userId, String role) {
                String insert = "INSERT INTO " + Constant.TEAM_MEMBERS_TABLE + " (" +
                        Constant.TEAM_MEMBERS_TEAM_ID + ", " + Constant.TEAM_MEMBERS_USER_ID + ", " + Constant.TEAM_MEMBERS_ROLE +
                        ") VALUES (?, ?, ?)";

                try (PreparedStatement prSt = getDbConnection().prepareStatement(insert)) {
                    prSt.setInt(1, teamId);   // ID команды
                    prSt.setInt(2, userId);   // ID пользователя
                    prSt.setString(3, role);  // Роль пользователя в команде
                    prSt.executeUpdate();     // Выполняем SQL-запрос
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace(); // Вывод ошибки
                }
            }

            // Метод для удаления пользователя из команды (удаляет запись из team_members)
            public void removeUserFromTeam(int teamId, int userId) {
                String delete = "DELETE FROM " + Constant.TEAM_MEMBERS_TABLE + " WHERE " +
                        Constant.TEAM_MEMBERS_TEAM_ID + "=? AND " + Constant.TEAM_MEMBERS_USER_ID + "=?";

                try (PreparedStatement prSt = getDbConnection().prepareStatement(delete)) {
                    prSt.setInt(1, teamId); // Указываем команду
                    prSt.setInt(2, userId); // Указываем пользователя
                    prSt.executeUpdate();   // Выполняем SQL-запрос
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace(); // Вывод ошибки
                }
            }

            // Метод для обновления имени команды
            public void updateTeamName(int teamId, String newName) {
                String query = "UPDATE " + Constant.TEAM_TABLE + " SET " + Constant.TEAM_NAME + " = ? WHERE id = ?";
                try (Connection conn = getDbConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, newName);
                    stmt.setInt(2, teamId);
                    stmt.executeUpdate();
                    System.out.println("Название команды обновлено: " + newName);
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }


            // Метод для создания проекта
            public boolean createProject(String projectName, int teamId) {
                String insert = "INSERT INTO " + Constant.PROJECT_TABLE + " (" +
                        Constant.PROJECT_NAME + ", " + Constant.PROJECT_TEAM_ID + ", " + Constant.PROJECT_STATUS +
                        ") VALUES (?, ?, ?)";

                try (Connection connection = getDbConnection();
                     PreparedStatement prSt = connection.prepareStatement(insert)) {

                    prSt.setString(1, projectName); // Устанавливаем название проекта
                    prSt.setInt(2, teamId);         // Устанавливаем ID команды
                    prSt.setString(3, "Planned");   // Устанавливаем статус "Planned"
                    int affectedRows = prSt.executeUpdate(); // Выполняем SQL-запрос

                    return affectedRows > 0; // Возвращаем true, если запись добавлена
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace(); // Вывод ошибки в случае неудачи
                    return false;
                }
            }

            //проверяет, существует ли команда с данным именем
            public boolean isProjectExists(String projectName) {
                String query = "SELECT COUNT(*) FROM " + Constant.PROJECT_TABLE + " WHERE " + Constant.PROJECT_NAME + " = ?";
                try (PreparedStatement pstmt = getDbConnection().prepareStatement(query)) {
                    pstmt.setString(1, projectName);
                    ResultSet rs = pstmt.executeQuery();
                    return rs.next() && rs.getInt(1) > 0;
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            public String getProjectStatus(String projectName) {
                String query = "SELECT CAST(" + Constant.PROJECT_STATUS + " AS CHAR) AS status FROM " + Constant.PROJECT_TABLE +
                        " WHERE LOWER(" + Constant.PROJECT_NAME + ") = LOWER(?)";

                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, projectName.trim());
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getString("status");
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return "Unknown"; // Возвращаем "Unknown", если статус не найден
            }

            public boolean deleteProjectByName(String projectName) {
                String query = "DELETE FROM " + Constant.PROJECT_TABLE + " WHERE " + Constant.PROJECT_NAME + " = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, projectName);
                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            public boolean updateProject(String oldName, String newName, String newStatus) {
                String query = "UPDATE " + Constant.PROJECT_TABLE +
                        " SET " + Constant.PROJECT_NAME + " = ?, " + Constant.PROJECT_STATUS + " = ?" +
                        " WHERE " + Constant.PROJECT_NAME + " = ?";

                try (Connection connection = getDbConnection();
                     PreparedStatement stmt = connection.prepareStatement(query)) {

                    stmt.setString(1, newName);
                    stmt.setString(2, newStatus);
                    stmt.setString(3, oldName);

                    int rowsAffected = stmt.executeUpdate();
                    return rowsAffected > 0;

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            public int getProjectIdByName(String projectName) {
                String query = "SELECT " + Constant.PROJECT_ID + " FROM " + Constant.PROJECT_TABLE + " WHERE " + Constant.PROJECT_NAME + " = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, projectName);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(Constant.PROJECT_ID);
                    } else {
                        return -1; // Возвращаем -1, если проект не найден
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return -1;
                }
            }


            // Метод для загрузки проектов по teamId
            public ObservableList<String> loadProjectsByTeamId(int teamId) {
                String query = "SELECT " + Constant.PROJECT_NAME + " FROM " + Constant.PROJECT_TABLE +
                        " WHERE " + Constant.PROJECT_TEAM_ID + " = ?";
                ObservableList<String> projectList = FXCollections.observableArrayList();

                try (Connection connection = getDbConnection();
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


            // Метод для загрузки участников команды по teamId
            public ObservableList<User> loadTeamMembersByTeamId(int teamId) {
                String query = "SELECT u.* FROM " + Constant.TEAM_MEMBERS_TABLE + " tm " +
                        "JOIN " + Constant.USER_TABLE + " u ON tm." + Constant.TEAM_MEMBERS_USER_ID + " = u." + Constant.USER_ID +
                        " WHERE tm." + Constant.TEAM_MEMBERS_TEAM_ID + " = ?";
                ObservableList<User> userList = FXCollections.observableArrayList();

                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

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

            public boolean createTask(String title, int projectId, LocalDate deadline, int assigneeId, int createdById) {
                String insert = "INSERT INTO tasks (project_id, assigned_to, title, status, created_by, deadline) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                try (Connection connection = getDbConnection();
                     PreparedStatement prSt = connection.prepareStatement(insert)) {

                    prSt.setInt(1, projectId);
                    prSt.setInt(2, assigneeId);
                    prSt.setString(3, title);
                    prSt.setString(4, "To Do"); // Устанавливаем статус "To Do"
                    prSt.setInt(5, createdById);
                    prSt.setDate(6, java.sql.Date.valueOf(deadline));

                    int affectedRows = prSt.executeUpdate();
                    return affectedRows > 0;

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            // Метод для загрузки задач по projectId из базы данных
            public ObservableList<Task> loadTasksByProjectId(int projectId) {
                String query = "SELECT * FROM tasks WHERE project_id = ?";
                ObservableList<Task> taskList = FXCollections.observableArrayList();

                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, projectId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        Task task = new Task(
                                rs.getInt("id"),                   // Используем id из базы данных
                                rs.getInt("project_id"),
                                rs.getString("assigned_to"),
                                rs.getString("title"),
                                rs.getString("status"),
                                rs.getString("created_by"),
                                rs.getDate("deadline").toLocalDate()
                        );
                        taskList.add(task);
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return taskList;
            }

            public boolean deleteTaskByName(String taskName) {
                String query = "DELETE FROM tasks WHERE title = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, taskName);
                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            public int getCreatedTaskCount(int userId) {
                String query = "SELECT COUNT(*) FROM tasks WHERE created_by = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            public int getCompletedTaskCount(int userId) {
                String query = "SELECT COUNT(*) FROM tasks WHERE assigned_to = ? AND status = 'Completed'";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            public int getOverdueTaskCount(int userId) {
                String query = "SELECT COUNT(*) FROM tasks WHERE assigned_to = ? AND deadline < CURDATE() AND status != 'Completed'";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            public int getCreatedTaskByProjectId(int projectId) {
                String query = "SELECT COUNT(*) FROM tasks WHERE project_id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, projectId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return 0;
            }

            public Task getTaskByName(String taskName) {
                String query = "SELECT * FROM tasks WHERE title = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, taskName);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        Task task = new Task(
                                rs.getInt("id"),
                                rs.getInt("project_id"),
                                rs.getString("assigned_to"),
                                rs.getString("title"),
                                rs.getString("status"),
                                rs.getString("created_by"),
                                rs.getDate("deadline").toLocalDate()
                        );
                        return task;
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null; // Возвращаем null, если задача не найдена
            }

            public boolean updateTask(int taskId, String newTitle, String newStatus, LocalDate newDeadline, int newAssigneeId) {
                String query = "UPDATE tasks SET title = ?, status = ?, deadline = ?, assigned_to = ? WHERE id = ?";

                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, newTitle);
                    pstmt.setString(2, newStatus);
                    pstmt.setDate(3, java.sql.Date.valueOf(newDeadline));
                    pstmt.setInt(4, newAssigneeId);
                    pstmt.setInt(5, taskId);

                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            //телефон по id
            public String getUserPhoneByUserId(int userId) {
                String query = "SELECT phone FROM user_contacts WHERE user_id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getString("phone"); // Возвращаем телефон
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null; // Если телефон не найден
            }

            public void setUserPhoneByUserId(int userId, String phone) {
                String checkExistence = "SELECT COUNT(*) FROM user_contacts WHERE user_id = ?";
                String insert = "INSERT INTO user_contacts (user_id, phone) VALUES (?, ?)";
                String update = "UPDATE user_contacts SET phone = ? WHERE user_id = ?";

                try (Connection connection = getDbConnection();
                     PreparedStatement checkStmt = connection.prepareStatement(checkExistence);
                     PreparedStatement insertStmt = connection.prepareStatement(insert);
                     PreparedStatement updateStmt = connection.prepareStatement(update)) {

                    checkStmt.setInt(1, userId);
                    ResultSet resultSet = checkStmt.executeQuery();
                    resultSet.next();
                    int count = resultSet.getInt(1);

                    if (count == 0) {
                        // Если записи нет, выполняем INSERT
                        insertStmt.setInt(1, userId);
                        insertStmt.setString(2, phone);
                        insertStmt.executeUpdate();
                    } else {
                        // Если запись есть, выполняем UPDATE
                        updateStmt.setString(1, phone);
                        updateStmt.setInt(2, userId);
                        updateStmt.executeUpdate();
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            //адрес по id
            public String getUserAddressByUserId(int userId) {
                String query = "SELECT address FROM user_contacts WHERE user_id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getString("address"); // Возвращаем адрес
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null; // Если адрес не найден
            }

            public void setUserAddressByUserId(int userId, String address) {
                String checkExistence = "SELECT COUNT(*) FROM user_contacts WHERE user_id = ?";
                String insert = "INSERT INTO user_contacts (user_id, address) VALUES (?, ?)";
                String update = "UPDATE user_contacts SET address = ? WHERE user_id = ?";

                try (Connection connection = getDbConnection();
                     PreparedStatement checkStmt = connection.prepareStatement(checkExistence);
                     PreparedStatement insertStmt = connection.prepareStatement(insert);
                     PreparedStatement updateStmt = connection.prepareStatement(update)) {

                    checkStmt.setInt(1, userId);
                    ResultSet resultSet = checkStmt.executeQuery();
                    resultSet.next();
                    int count = resultSet.getInt(1);

                    if (count == 0) {
                        // Если записи нет, выполняем INSERT
                        insertStmt.setInt(1, userId);
                        insertStmt.setString(2, address);
                        insertStmt.executeUpdate();
                    } else {
                        // Если запись есть, выполняем UPDATE
                        updateStmt.setString(1, address);
                        updateStmt.setInt(2, userId);
                        int rowsAffected = updateStmt.executeUpdate();
                        System.out.println("Updated address for user ID " + userId + ": " + address + ". Rows affected: " + rowsAffected);
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            public void updateUser(User user) {
                String update = "UPDATE " + Constant.USER_TABLE + " SET " +
                        Constant.USER_FIRSTNAME + " = ?, " +
                        Constant.USER_LASTNAME + " = ?, " +
                        Constant.USER_EMAIL + " = ?, " +
                        Constant.USER_GENDER + " = ?, " +
                        Constant.USER_ROLE + " = ? WHERE " +
                        Constant.USER_ID + " = ?";

                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(update)) {

                    pstmt.setString(1, user.getFirstName());
                    pstmt.setString(2, user.getLastName());
                    pstmt.setString(3, user.getUserEmail());
                    pstmt.setString(4, user.getUserGender());
                    pstmt.setString(5, user.getUserRole());
                    pstmt.setInt(6, user.getId());
                    pstmt.executeUpdate();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // Метод для получения пароля пользователя по ID
            public String getPassByUserId(int userId) {
                String query = "SELECT password FROM users WHERE id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setInt(1, userId);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        return rs.getString("password");
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }

            // Метод для обновления пароля пользователя
            public void setPassByUserId(int userId, String newPassword) {
                String update = "UPDATE users SET password = ? WHERE id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(update)) {

                    pstmt.setString(1, newPassword);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            public boolean deleteUserById(int userId) {
                String deleteQuery = "DELETE FROM users WHERE id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {

                    pstmt.setInt(1, userId);
                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return false;
            }

            public List<Task> getTasksByUserId(int userId) {
                List<Task> tasks = new ArrayList<>();
                String selectQuery = "SELECT * FROM tasks WHERE assigned_to = ? OR created_by = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {

                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, userId);
                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        Task task = new Task(
                                rs.getInt("id"),
                                rs.getInt("project_id"),
                                rs.getString("assigned_to"),
                                rs.getString("title"),
                                rs.getString("status"),
                                rs.getString("created_by"),
                                rs.getDate("deadline").toLocalDate()
                        );
                        tasks.add(task);
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return tasks;
            }

            public User getAdminOfTeam() {
                String selectQuery = "SELECT * FROM users WHERE role = 'Admin' LIMIT 1";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {

                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        User admin = new User();
                        admin.setId(rs.getInt("id"));
                        admin.setFirstName(rs.getString("firstname"));
                        admin.setLastName(rs.getString("lastname"));
                        admin.setUserEmail(rs.getString("email"));
                        admin.setUserPassword(rs.getString("password"));
                        admin.setUserGender(rs.getString("gender"));
                        admin.setUserRole(rs.getString("role"));
                        return admin;
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }

            public void reassignTask(int taskId, int newAssigneeId, int currentUserId) {
                String updateQuery = "UPDATE tasks SET assigned_to = ?, created_by = ? WHERE id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {

                    pstmt.setInt(1, newAssigneeId);
                    pstmt.setInt(2, newAssigneeId);
                    pstmt.setInt(3, taskId);
                    pstmt.executeUpdate();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            public void updateAdminRole(int newAdminId) {
                String updateNewAdminQuery = "UPDATE users SET role = 'Admin' WHERE id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmtNewAdmin = connection.prepareStatement(updateNewAdminQuery)) {

                    pstmtNewAdmin.setInt(1, newAdminId);
                    pstmtNewAdmin.executeUpdate();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            public void updateTeamAdmin(int teamId, int newAdminId) {
                String updateQuery = "UPDATE teams SET admin_id = ? WHERE id = ?";
                try (Connection connection = getDbConnection();
                     PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {

                    pstmt.setInt(1, newAdminId);
                    pstmt.setInt(2, teamId);
                    pstmt.executeUpdate();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
}
