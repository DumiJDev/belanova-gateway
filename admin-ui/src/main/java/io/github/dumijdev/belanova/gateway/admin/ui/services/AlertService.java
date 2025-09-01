package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.Alert;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AlertService {

    // In-memory storage for demo purposes - in production, this would be a database
    private final Map<String, Alert> alerts = new ConcurrentHashMap<>();

    public AlertService() {
        // Add some sample alerts for demo
        createSampleAlerts();
    }

    public List<Alert> getAllAlerts() {
        return alerts.values().stream()
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
            .collect(Collectors.toList());
    }

    public Alert getAlertById(String id) {
        return alerts.get(id);
    }

    public Alert createAlert(String title, String description, String severity, String source) {
        Alert alert = Alert.create(title, description, severity, source);
        alerts.put(alert.id(), alert);
        return alert;
    }

    public Alert updateAlert(String id, String title, String description, String severity, String source) {
        Alert existing = alerts.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Alert not found");
        }

        Alert updated = existing.withTitle(title)
                               .withDescription(description)
                               .withSeverity(severity);
        alerts.put(id, updated);
        return updated;
    }

    public void updateAlertStatus(String id, String status) {
        Alert existing = alerts.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Alert not found");
        }

        Alert updated = existing.withStatus(status);
        alerts.put(id, updated);
    }

    public void resolveAlert(String id, String resolution) {
        Alert existing = alerts.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Alert not found");
        }

        Alert updated = existing.withStatus("resolved").withResolution(resolution);
        alerts.put(id, updated);
    }

    public void deleteAlert(String id) {
        if (!alerts.containsKey(id)) {
            throw new IllegalArgumentException("Alert not found");
        }
        alerts.remove(id);
    }

    public List<Alert> getAlertsByStatus(String status) {
        return alerts.values().stream()
            .filter(alert -> alert.status().equals(status))
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
            .collect(Collectors.toList());
    }

    public List<Alert> getAlertsBySeverity(String severity) {
        return alerts.values().stream()
            .filter(alert -> alert.severity().equals(severity))
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
            .collect(Collectors.toList());
    }

    public void clearResolvedAlerts() {
        alerts.entrySet().removeIf(entry -> "resolved".equals(entry.getValue().status()));
    }

    public Map<String, Long> getAlertStats() {
        return Map.of(
            "total", (long) alerts.size(),
            "active", alerts.values().stream().filter(a -> "active".equals(a.status())).count(),
            "acknowledged", alerts.values().stream().filter(a -> "acknowledged".equals(a.status())).count(),
            "resolved", alerts.values().stream().filter(a -> "resolved".equals(a.status())).count()
        );
    }

    private void createSampleAlerts() {
        createAlert("High Error Rate Detected", "The error rate for /api/users endpoint has exceeded 5% threshold", "High", "gateway-service");
        createAlert("Backend Service Unhealthy", "Backend service backend-1 is not responding to health checks", "Critical", "health-monitor");
        createAlert("High Response Time", "Average response time has increased by 150% in the last hour", "Medium", "performance-monitor");
        createAlert("Rate Limit Exceeded", "Service user-service has exceeded its rate limit multiple times", "Low", "rate-limiter");

        // Update some statuses for demo
        alerts.values().stream()
            .filter(a -> a.title().contains("Rate Limit"))
            .findFirst()
            .ifPresent(a -> updateAlertStatus(a.id(), "acknowledged"));

        alerts.values().stream()
            .filter(a -> a.title().contains("Response Time"))
            .findFirst()
            .ifPresent(a -> updateAlertStatus(a.id(), "resolved"));
    }
}