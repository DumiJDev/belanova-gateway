package io.github.dumidev.belanova.gateway.gateway.config;

import io.github.dumidev.belanova.gateway.gateway.service.CacheService;
import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumijdev.belanova.gateway.common.model.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.method;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
@Slf4j
public class DynamicRouteLocator {

  private final CacheService cacheService;

  public DynamicRouteLocator(CacheService cacheService) {
    this.cacheService = cacheService;
  }

  @Bean
  public RouterFunction<ServerResponse> dynamicRoutes() {
    return cacheService.getBackends()
        .stream()
        .filter(Backend::isEnabled)
        .flatMap(backend -> backend.getServices().stream())
        .filter(Service::isEnabled)
        .map(service -> buildRoute(service.getBackend(), service))
        .reduce(RouterFunction::and)
        .orElse(null);
  }

  private RouterFunction<ServerResponse> buildRoute(Backend backend, Service service) {
    String uri = backend.isUseServiceDiscovery()
        ? "lb://" + backend.getServiceId()
        : backend.getBaseUrl();

    RequestPredicate predicate = path(service.getPath());
    if (service.getMethods() != null && !service.getMethods().isEmpty()) {
      var httpMethods = service.getMethods().stream().map(String::toUpperCase)
          .map(HttpMethod::valueOf).toArray(HttpMethod[]::new);
      predicate = predicate.and(method(httpMethods));
    }

    return GatewayRouterFunctions.route(backend.getId() + "-" + service.getId())
        .route(predicate, HandlerFunctions.http(uri))
        .before(rewritePath(service.getPath(),
            backend.getGeneralPath() != null && !backend.getGeneralPath().isBlank()
                ? backend.getGeneralPath() + service.getPath()
                : service.getPath()))
        .build();
  }
}
