package io.github.dumijdev.belanova.gateway.admin.ui.services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AnalyticsService {

    private final Random random = new Random();

    public Map<String, Object> getAnalyticsSummary(String timeRange, String service) {
        Map<String, Object> summary = new HashMap<>();

        // Mock data - in real implementation, this would come from metrics collection
        summary.put("totalRequests", 1247891 + random.nextInt(10000));
        summary.put("successRate", 98.7 + random.nextDouble() * 0.5);
        summary.put("averageResponseTime", 145 + random.nextInt(20));
        summary.put("errorRate", 1.3 - random.nextDouble() * 0.2);
        summary.put("p95ResponseTime", 234 + random.nextInt(30));
        summary.put("throughput", 1247 + random.nextInt(100));
        summary.put("activeConnections", 892 + random.nextInt(50));
        summary.put("bandwidthUsage", 2.4 + random.nextDouble() * 0.5);

        return summary;
    }

    public List<Map<String, Object>> getTopEndpoints(String timeRange, String service) {
        // Mock top endpoints data
        return List.of(
            Map.of("endpoint", "/api/users", "requests", 45231 + random.nextInt(1000), "avgResponseTime", 120 + random.nextInt(20)),
            Map.of("endpoint", "/api/orders", "requests", 38765 + random.nextInt(1000), "avgResponseTime", 150 + random.nextInt(20)),
            Map.of("endpoint", "/api/payments", "requests", 28934 + random.nextInt(1000), "avgResponseTime", 180 + random.nextInt(20)),
            Map.of("endpoint", "/api/notifications", "requests", 19876 + random.nextInt(1000), "avgResponseTime", 95 + random.nextInt(15))
        );
    }

    public List<Map<String, Object>> getErrorBreakdown(String timeRange, String service) {
        // Mock error breakdown data
        return List.of(
            Map.of("type", "4xx Client Errors", "count", 1247 + random.nextInt(100), "percentage", 78.5 + random.nextDouble() * 5),
            Map.of("type", "5xx Server Errors", "count", 234 + random.nextInt(50), "percentage", 14.7 + random.nextDouble() * 3),
            Map.of("type", "Timeout Errors", "count", 89 + random.nextInt(20), "percentage", 5.6 + random.nextDouble() * 2),
            Map.of("type", "Network Errors", "count", 45 + random.nextInt(15), "percentage", 2.8 + random.nextDouble() * 1)
        );
    }

    public List<Map<String, Object>> getTrafficTrends(String timeRange, String service) {
        // Mock traffic trends data
        return List.of(
            Map.of("timestamp", LocalDateTime.now().minusHours(23), "requests", 1200 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(22), "requests", 1350 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(21), "requests", 1180 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(20), "requests", 1420 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(19), "requests", 1380 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(18), "requests", 1590 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(17), "requests", 1650 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(16), "requests", 1720 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(15), "requests", 1680 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(14), "requests", 1820 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(13), "requests", 1890 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(12), "requests", 1950 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(11), "requests", 2100 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(10), "requests", 2250 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(9), "requests", 2180 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(8), "requests", 2350 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(7), "requests", 2420 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(6), "requests", 2380 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(5), "requests", 2520 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(4), "requests", 2680 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(3), "requests", 2750 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(2), "requests", 2820 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now().minusHours(1), "requests", 2890 + random.nextInt(200)),
            Map.of("timestamp", LocalDateTime.now(), "requests", 2950 + random.nextInt(200))
        );
    }

    public Map<String, Object> getServiceAnalytics(String serviceId, String timeRange) {
        Map<String, Object> serviceAnalytics = new HashMap<>();

        serviceAnalytics.put("serviceId", serviceId);
        serviceAnalytics.put("totalRequests", 50000 + random.nextInt(10000));
        serviceAnalytics.put("successRate", 97.5 + random.nextDouble() * 2);
        serviceAnalytics.put("averageResponseTime", 120 + random.nextInt(40));
        serviceAnalytics.put("errorCount", 1250 + random.nextInt(200));
        serviceAnalytics.put("uptime", 99.9 + random.nextDouble() * 0.1);

        return serviceAnalytics;
    }
}