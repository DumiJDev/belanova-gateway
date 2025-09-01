package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record LogEntry(
    LocalDateTime timestamp,
    String level,
    String logger,
    String message,
    String requestId,
    String stackTrace
) {
    public static LogEntry create(String level, String logger, String message) {
        return new LogEntry(LocalDateTime.now(), level, logger, message, null, null);
    }

    public static LogEntry create(String level, String logger, String message, String requestId) {
        return new LogEntry(LocalDateTime.now(), level, logger, message, requestId, null);
    }
}
