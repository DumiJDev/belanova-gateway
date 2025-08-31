package io.github.dumidev.belanova.gateway.gateway.controller;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumidev.belanova.gateway.gateway.service.CacheService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.List;

class GatewayConfigControllerTest {
    @Test
    void testGetBackends() {
        CacheService cacheService = Mockito.mock(CacheService.class);
        List<Backend> backends = List.of(new Backend());
        Mockito.when(cacheService.getBackends()).thenReturn(backends);
        GatewayConfigController controller = new GatewayConfigController(cacheService);
        WebTestClient client = WebTestClient.bindToController(controller).build();
        client.get().uri("/api/gateway/config/backends")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Backend.class).hasSize(1);
    }
}
