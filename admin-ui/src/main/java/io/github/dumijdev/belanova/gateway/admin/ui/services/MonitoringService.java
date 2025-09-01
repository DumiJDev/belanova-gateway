package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendHealthStatus;
import io.github.dumijdev.belanova.gateway.admin.ui.models.LogEntry;
import io.github.dumijdev.belanova.gateway.admin.ui.models.MetricInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    // In-memory storage for demo purposes
    private final Map<String, BackendHealthStatus> healthStatuses = new ConcurrentHashMap<>();
    private final Map<String, MetricInfo> metrics = new ConcurrentHashMap<>();
    private final Map<String, LogEntry> logs = new ConcurrentHashMap<>();

    public MonitoringService() {
        // Initialize with sample data
        createSampleData();
    }

    public List<BackendHealthStatus> getBackendHealthStatuses() {
        return healthStatuses.values().stream().collect(Collectors.toList());
    }

    public List<MetricInfo> getSystemMetrics() {
        return metrics.values().stream().collect(Collectors.toList());
    }

    public List<LogEntry> getLogEntries() {
        return logs.values().stream()
            .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
            .collect(Collectors.toList());
    }

    public void forceHealthCheck(String backendId) {
        // Simulate health check
        BackendHealthStatus existing = healthStatuses.get(backendId);
        if (existing != null) {
            BackendHealthStatus updated = existing.withStatus("healthy");
            healthStatuses.put(backendId, updated);
        }
    }

    public void clearLogs() {
        logs.clear();
    }

    private void createSampleData() {
        // Sample health statuses
        healthStatuses.put("backend-1", BackendHealthStatus.create("backend-1", "User Service"));
        healthStatuses.put("backend-2", BackendHealthStatus.create("backend-2", "Order Service"));
        healthStatuses.put("backend-3", BackendHealthStatus.create("backend-3", "Payment Service"));

        // Sample metrics
        metrics.put("cpu", MetricInfo.create("CPU Usage", 45.2, "%", "Current CPU utilization"));
        metrics.put("memory", MetricInfo.create("Memory Usage", 2.1, "GB", "Current memory usage"));
        metrics.put("disk", MetricInfo.create("Disk Usage", 234.5, "MB", "Current disk I/O"));
        metrics.put("network", MetricInfo.create("Network I/O", 15.7, "MB/s", "Current network traffic"));

        // Sample logs
        logs.put("log-1", LogEntry.create("INFO", "com.belanova.gateway", "Gateway started successfully"));
        logs.put("log-2", LogEntry.create("WARN", "com.belanova.gateway", "High memory usage detected"));
        logs.put("log-3", LogEntry.create("ERROR", "com.belanova.backend", "Connection timeout to database"));
        logs.put("log-4", LogEntry.create("INFO", "com.belanova.gateway", "Route configuration updated"));
        logs.put("log-5", LogEntry.create("DEBUG", "com.belanova.plugin", "Plugin authentication loaded"));
    }
}
