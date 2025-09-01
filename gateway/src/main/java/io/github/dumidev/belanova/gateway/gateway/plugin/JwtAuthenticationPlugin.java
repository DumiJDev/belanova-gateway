package io.github.dumidev.belanova.gateway.gateway.plugin;

import io.github.dumijdev.belanova.gateway.common.model.*;
import io.github.dumijdev.belanova.gateway.common.plugin.GatewayPlugin;
import io.github.dumijdev.belanova.gateway.common.plugin.PluginPhase;
import io.github.dumidev.belanova.gateway.gateway.service.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationPlugin implements GatewayPlugin {

    private final JwtTokenValidator jwtValidator;

    @Override
    public String getName() {
        return "jwt-auth";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public PluginPhase getPhase() {
        return PluginPhase.AUTH;
    }

    @Override
    public void execute(GatewayMvcContext context) throws PluginException {
        // For now, we'll work with HttpServletRequest
        // In a full implementation, this would be adapted to work with the gateway context
        log.debug("JWT Authentication plugin executed");

        // This is a placeholder implementation
        // In a real scenario, you would extract the JWT token from the request
        // and validate it using the jwtValidator

        // For demonstration purposes, we'll assume authentication passes
        // In production, you would:
        // 1. Extract Authorization header
        // 2. Validate JWT token
        // 3. Set user context
        // 4. Handle authentication failures
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Map.of(
            "secret", "${jwt.secret:default-secret}",
            "issuer", "${jwt.issuer:belanova-gateway}",
            "audience", "${jwt.audience:belanova-api}",
            "enabled", true
        );
    }

    @Override
    public void validateConfiguration(Map<String, Object> config) throws ConfigurationException {
        if (!config.containsKey("secret") || config.get("secret") == null) {
            throw new ConfigurationException("JWT secret is required");
        }

        String secret = config.get("secret").toString();
        if (secret.length() < 32) {
            throw new ConfigurationException("JWT secret must be at least 32 characters long");
        }
    }
}