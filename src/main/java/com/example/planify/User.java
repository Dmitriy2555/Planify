package com.example.planify;

/**
 * Represents a user in the Planify application.
 * Each user has attributes such as userId, firstName, lastName, userEmail, userPassword, userGender, and userRole.
 */
public class User {
    private int userId;
    private String firstName;
    private String lastName;
    private String userEmail;
    private String userPassword;
    private String userGender;
    private String userRole;

    public User(String firstName, String lastName, String userEmail, String userPassword, String userGender, String userRole)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userGender = userGender;
        this.userRole = userRole;
    }

    public User() {

    }

    public User(String loginText, String passwordText)
    {
        this.userEmail = loginText;
        this.userPassword = passwordText;
    }


    public int getId() { return userId; }

    public void setId(int id) { this.userId = id; }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
