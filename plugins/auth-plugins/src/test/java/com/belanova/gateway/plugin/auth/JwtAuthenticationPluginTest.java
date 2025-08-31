package com.belanova.gateway.plugin.auth;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.server.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationPluginTest {

    @Test
    void testApplyAddsUserIdHeader() {
        JwtAuthenticationPlugin plugin = new JwtAuthenticationPlugin();
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test.jwt.token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = Mockito.mock(GatewayFilterChain.class);
        Mockito.when(chain.filter(Mockito.any())).thenReturn(Mono.empty());

        plugin.apply(exchange, chain).block();

        assertEquals("user-from-jwt", exchange.getRequest().getHeaders().getFirst("X-User-Id"));
    }
}
