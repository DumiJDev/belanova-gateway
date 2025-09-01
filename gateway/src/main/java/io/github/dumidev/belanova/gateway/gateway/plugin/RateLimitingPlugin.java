package io.github.dumidev.belanova.gateway.gateway.plugin;

import io.github.dumijdev.belanova.gateway.common.model.*;
import io.github.dumijdev.belanova.gateway.common.plugin.GatewayPlugin;
import io.github.dumijdev.belanova.gateway.common.plugin.PluginPhase;
import io.github.dumidev.belanova.gateway.gateway.config.RateLimitConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitingPlugin implements GatewayPlugin {

    private final RateLimitConfiguration config;
    private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStartTimes = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "rate-limit";
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public boolean isEnabled() {
        return config != null && config.isEnabled();
    }

    @Override
    public PluginPhase getPhase() {
        return PluginPhase.PRE_REQUEST;
    }

    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        if (!isEnabled()) {
            return;
        }

        String clientId = extractClientId(context);
        String key = "rate_limit:" + clientId;

        // Use in-memory sliding window algorithm
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - config.getWindowSizeMs();

        // Reset window if needed
        Long lastWindowStart = windowStartTimes.get(key);
        if (lastWindowStart == null || currentTime - lastWindowStart >= config.getWindowSizeMs()) {
            requestCounts.put(key, new AtomicLong(0));
            windowStartTimes.put(key, currentTime);
        }

        // Get current count
        AtomicLong currentCount = requestCounts.computeIfAbsent(key, k -> new AtomicLong(0));

        if (currentCount.get() >= config.getMaxRequests()) {
            throw new RateLimitExceededException(
                "Rate limit exceeded for client: " + clientId,
                String.valueOf(config.getMaxRequests()),
                "0"
            );
        }

        // Increment request count
        currentCount.incrementAndGet();

        log.debug("Rate limit check passed for client: {} (count: {})", clientId, currentCount.get());
    }

    private String extractClientId(GatewayMvcContext context) {
        // For now, return a default client ID
        // In a real implementation, you would extract from JWT token or IP address
        return "default-client";
    }

    @Override
    public Map<String, Object> getConfiguration() {
        if (config != null) {
            return Map.of(
                "maxRequests", config.getMaxRequests(),
                "windowSizeMs", config.getWindowSizeMs(),
                "enabled", config.isEnabled()
            );
        }
        return Map.of(
            "maxRequests", 100,
            "windowSizeMs", 60000,
            "enabled", false
        );
    }

    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        Object maxRequests = config.get("maxRequests");
        if (maxRequests == null || !(maxRequests instanceof Number) ||
            ((Number) maxRequests).intValue() <= 0) {
            throw new ConfigurationException("maxRequests must be a positive number");
        }

        Object windowSizeMs = config.get("windowSizeMs");
        if (windowSizeMs == null || !(windowSizeMs instanceof Number) ||
            ((Number) windowSizeMs).longValue() <= 0) {
            throw new ConfigurationException("windowSizeMs must be a positive number");
        }
    }
}