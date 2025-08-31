package com.belanova.gateway.plugin.auth;

import com.belanova.gateway.common.plugin.GatewayPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationPlugin implements GatewayPlugin {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public String getName() {
        return "JWT Authentication Plugin";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Mono<Void> apply(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            // In a real implementation, you would validate the JWT and extract the user ID
            String userId = "user-from-jwt";
            exchange.getRequest().mutate().header(USER_ID_HEADER, userId).build();
            log.info("Authenticated user {}", userId);
        }

        return chain.filter(exchange);
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Collections.emptyMap();
    }
}
