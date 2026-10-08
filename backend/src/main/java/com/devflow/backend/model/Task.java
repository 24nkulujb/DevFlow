package com.devflow.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long creatorId;
    private Long assigneeId;
    private LocalDate dueDate;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String creatorName;
    private String assigneeName;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long value) {
        projectId = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String value) {
        priority = value;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long value) {
        creatorId = value;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long value) {
        assigneeId = value;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate value) {
        dueDate = value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer value) {
        version = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime value) {
        createdAt = value;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime value) {
        updatedAt = value;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String value) {
        creatorName = value;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String value) {
        assigneeName = value;
    }
}
