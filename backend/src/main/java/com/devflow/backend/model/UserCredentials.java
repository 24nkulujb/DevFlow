package com.devflow.backend.model;

public record UserCredentials(Long id, String username, String passwordHash, String displayName) {}
