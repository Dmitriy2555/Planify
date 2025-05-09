package com.example.planify;

/**
 * The Session class implements the Singleton pattern to store information
 * about the currently authenticated user in the application.
 */
public class Session {
    // The single instance of the class
    private static Session instance;

    // Field to store information about the current user
    private User loggedInUser;

    // The last opened project
    private String lastOpenedProject;

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private Session() {}

    /**
     * Method to get the single instance of the class.
     * Implements thread-safe access to the object.
     *
     * @return the instance of the Session class
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Returns the currently authenticated user.
     *
     * @return the User object representing the current user, or null if no user is authenticated
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Sets the user as the currently authenticated user.
     *
     * @param user the User object representing the authenticated user
     */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    /**
     * Clears the information about the current user (logout).
     */
    public void clearSession() {
        loggedInUser = null;
    }

    /**
     * Sets the name of the last opened project.
     *
     * @param projectName the name of the last opened project
     */
    public void setLastOpenedProject(String projectName) {
        this.lastOpenedProject = projectName;
    }

    /**
     * Returns the name of the last opened project.
     *
     * @return the name of the last opened project
     */
    public String getLastOpenedProject() {
        return lastOpenedProject;
    }
}
