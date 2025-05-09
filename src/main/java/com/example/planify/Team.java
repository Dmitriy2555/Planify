package com.example.planify;

/**
 * Represents a team in the Planify application.
 * Each team has a unique identifier, a name, and an administrator.
 */
public class Team {
    private int id;
    private String team_name;
    private int admin_id;

    public Team(int id, String team_name, int admin_id) {
        this.id = id;
        this.team_name = team_name;
        this.admin_id = admin_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return team_name;
    }

    public void setName(String team_name) {
        this.team_name = team_name;
    }

    public int getAdminId() {
        return admin_id;
    }

    public void setAdminId(int admin_id) {
        this.admin_id = admin_id;
    }
}
