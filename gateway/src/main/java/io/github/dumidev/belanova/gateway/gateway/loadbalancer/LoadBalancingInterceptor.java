package io.github.dumidev.belanova.gateway.gateway.loadbalancer;

import io.github.dumidev.belanova.gateway.gateway.service.CacheService;
import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URI;
import java.util.Optional;

@Component
@Slf4j
public class LoadBalancingInterceptor implements HandlerInterceptor, Ordered {

  private final CacheService cacheService;
  private final LoadBalancer loadBalancer;
  private final GatewayMvcProperties gatewayProperties;

  public LoadBalancingInterceptor(CacheService cacheService,
                                  LoadBalancer loadBalancer,
                                  GatewayMvcProperties gatewayProperties) {
    this.cacheService = cacheService;
    this.loadBalancer = loadBalancer;
    this.gatewayProperties = gatewayProperties;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // Obter a rota atual do atributo da requisição
    String routeId = (String) request.getAttribute(MvcUtils.GATEWAY_ROUTE_ID_ATTR);

    if (routeId == null) {
      return true;
    }

    Optional<Backend> backendOpt = cacheService.getBackends().stream()
        .filter(backend -> backend.getServices() != null &&
            backend.getServices().stream()
                .anyMatch(s -> (backend.getId() + "-" + s.getId()).equals(routeId)))
        .findFirst();

    if (backendOpt.isEmpty()) {
      return true;
    }

    Backend backend = backendOpt.get();

    if (backend.getUpstreams() == null || backend.getUpstreams().isEmpty()) {
      return true;
    }

    Optional<Upstream> selectedUpstream =
        loadBalancer.select(backend.getUpstreams(), request);

    if (selectedUpstream.isEmpty()) {
      log.error("No healthy upstreams available for backend {}", backend.getId());
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return false;
    }

    String target = "http://" + selectedUpstream.get().getHost() + ":" + selectedUpstream.get().getPort();
    URI newUri = URI.create(target);

    // Armazenar o URI de destino como um atributo da requisição
    request.setAttribute(MvcUtils.GATEWAY_REQUEST_URL_ATTR, newUri);
    log.debug("Load balancing to {}", newUri);

    return true;
  }

  @Override
  public int getOrder() {
    return 10101; // Mesma ordem do filtro original
  }
}