package com.example.planify;

import java.time.LocalDate;

public class Task {
    private int id;
    private int projectId;
    private String assignedTo;
    private String title;
    private String status;
    private String createdBy;
    private LocalDate deadline;

    // Конструктор
    public Task(int id, int projectId, String assignedTo, String title, String status, String createdBy, LocalDate deadline) {
        this.id = id;
        this.projectId = projectId;
        this.assignedTo = assignedTo;
        this.title = title;
        this.status = status;
        this.createdBy = createdBy;
        this.deadline = deadline;
    }

    // Конструктор без id (для создания новых задач)
    public Task(int projectId, String assignedTo, String title, String status, String createdBy, LocalDate deadline) {
        this.projectId = projectId;
        this.assignedTo = assignedTo;
        this.title = title;
        this.status = status;
        this.createdBy = createdBy;
        this.deadline = deadline;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getAssigneeName() {
        return assignedTo;
    }
}
