package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record Alert(
    String id,
    String title,
    String description,
    String severity,
    String status,
    String source,
    String resolution,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static Alert create(String title, String description, String severity, String source) {
        String id = java.util.UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return new Alert(id, title, description, severity, "active", source, null, now, now);
    }

    public Alert withStatus(String newStatus) {
        return new Alert(id, title, description, severity, newStatus, source, resolution, createdAt, LocalDateTime.now());
    }

    public Alert withResolution(String newResolution) {
        return new Alert(id, title, description, severity, status, source, newResolution, createdAt, LocalDateTime.now());
    }

    public Alert withTitle(String newTitle) {
        return new Alert(id, newTitle, description, severity, status, source, resolution, createdAt, LocalDateTime.now());
    }

    public Alert withDescription(String newDescription) {
        return new Alert(id, title, newDescription, severity, status, source, resolution, createdAt, LocalDateTime.now());
    }

    public Alert withSeverity(String newSeverity) {
        return new Alert(id, title, description, newSeverity, status, source, resolution, createdAt, LocalDateTime.now());
    }
}