package com.belanova.gateway.plugin.observability;

import com.belanova.gateway.common.plugin.GatewayPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
public class LoggingPlugin implements GatewayPlugin {

    @Override
    public String getName() {
        return "Logging Plugin";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Mono<Void> apply(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Request path: {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Collections.emptyMap();
    }
}
