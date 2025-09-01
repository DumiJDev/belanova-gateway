package io.github.dumidev.belanova.gateway.gateway.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dumijdev.belanova.gateway.common.model.*;
import io.github.dumijdev.belanova.gateway.common.plugin.GatewayPlugin;
import io.github.dumijdev.belanova.gateway.common.plugin.PluginPhase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestLoggingPlugin implements GatewayPlugin {

    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "request-logging";
    }

    @Override
    public int getOrder() {
        return 1000;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public PluginPhase getPhase() {
        return PluginPhase.POST_REQUEST;
    }

    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        try {
            RequestLogEntry logEntry = RequestLogEntry.builder()
                    .timestamp(Instant.now())
                    .method("GET") // Placeholder - would be extracted from actual request
                    .path("/api/test") // Placeholder - would be extracted from actual request
                    .queryString(null)
                    .clientIp("127.0.0.1") // Placeholder - would be extracted from actual request
                    .userAgent("Belanova-Gateway/1.0") // Placeholder
                    .statusCode(200) // Placeholder - would be extracted from response
                    .responseSize(1024L) // Placeholder
                    .processingTimeMs(150L) // Placeholder
                    .backend("test-backend") // Placeholder
                    .service("test-service") // Placeholder
                    .userId("anonymous") // Placeholder
                    .build();

            log.info("Gateway Request: {}", objectMapper.writeValueAsString(logEntry));

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize log entry", e);
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "logLevel", "INFO",
            "includeHeaders", false,
            "includeBody", false,
            "enabled", true
        );
    }

    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        // No validation needed for this plugin
    }
}