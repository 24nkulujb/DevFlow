package com.devflow.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Project {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String role;
    private Long memberCount;
    private Long taskCount;
    private Long doneCount;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        id = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
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

    public String getRole() {
        return role;
    }

    public void setRole(String value) {
        role = value;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long value) {
        memberCount = value;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long value) {
        taskCount = value;
    }

    public Long getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(Long value) {
        doneCount = value;
    }
}
