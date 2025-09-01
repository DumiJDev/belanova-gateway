package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record Consumer(
    String id,
    String username,
    String customId,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static Consumer create(String username, String customId) {
        String id = java.util.UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return new Consumer(id, username, customId, "active", now, now);
    }

    public Consumer withStatus(String newStatus) {
        return new Consumer(id, username, customId, newStatus, createdAt, LocalDateTime.now());
    }

    public Consumer withUsername(String newUsername) {
        return new Consumer(id, newUsername, customId, status, createdAt, LocalDateTime.now());
    }

    public Consumer withCustomId(String newCustomId) {
        return new Consumer(id, username, newCustomId, status, createdAt, LocalDateTime.now());
    }
}