package com.example.planify;

/**
 * Class containing constant values for database table and column names.
 * These constants are used to interact with the database in a consistent manner.
 */
public class Constant {
    // Users table
    public static final String USER_TABLE = "Users";
    public static final String USER_ID = "id";
    public static final String USER_FIRSTNAME = "firstname";
    public static final String USER_LASTNAME = "lastname";
    public static final String USER_EMAIL = "email";
    public static final String USER_ROLE = "role";
    public static final String USER_PASS = "password";
    public static final String USER_GENDER = "gender";

    // Teams table
    public static final String TEAM_TABLE = "teams";
    public static final String TEAM_ID = "id";
    public static final String TEAM_NAME = "team_name";
    public static final String TEAM_ADMIN_ID = "admin_id";

    // Team members table
    public static final String TEAM_MEMBERS_TABLE = "team_members";
    public static final String TEAM_MEMBERS_ID = "id";
    public static final String TEAM_MEMBERS_TEAM_ID = "team_id";
    public static final String TEAM_MEMBERS_USER_ID = "user_id";
    public static final String TEAM_MEMBERS_ROLE = "role";

    // Projects table
    public static final String PROJECT_TABLE = "projects";
    public static final String PROJECT_ID = "id";
    public static final String PROJECT_TEAM_ID = "team_id";
    public static final String PROJECT_NAME = "name";
    public static final String PROJECT_STATUS = "status";
    public static final String CREATION_DATE = "creation_date";

    // Tasks table
    public static final String TASK_TABLE = "tasks";
    public static final String TASK_ID = "id";
    public static final String TASK_PROJECT_ID = "project_id";
    public static final String TASK_ASSIGNED_TO = "assigned_to";
    public static final String TASK_TITLE = "title";
    public static final String TASK_STATUS = "status";
    public static final String TASK_CREATED_BY = "created_by";
    public static final String TASK_DEADLINE = "deadline";

    // Avatar
    public static final String USER_AVATAR_PATH = "avatar_path";
    public static final String USER_CONTACTS_TABLE = "user_contacts";
    public static final String USER_CONTACTS_USER_ID = "user_id";
}
