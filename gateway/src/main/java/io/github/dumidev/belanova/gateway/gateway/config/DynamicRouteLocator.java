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
    log.info("Building dynamic routes...");
    var backends = cacheService.getBackends();
    log.info("Found {} backends", backends.size());

    return backends.stream()
        .filter(Backend::isEnabled)
        .peek(backend -> log.info("Processing backend: {}", backend.getName()))
        .filter(backend -> backend.getServices() != null && !backend.getServices().isEmpty())
        .flatMap(backend -> backend.getServices().stream()
            .filter(Service::isEnabled)
            .peek(service -> log.info("Processing service: {}", service.getName()))
            .map(service -> buildRoute(backend, service))
            .filter(java.util.Objects::nonNull))
        .reduce(RouterFunction::and)
        .orElse(null);
  }

  private RouterFunction<ServerResponse> buildRoute(Backend backend, Service service) {
    String uri;
    if (backend.isUseServiceDiscovery() && backend.getServiceId() != null) {
      uri = "lb://" + backend.getServiceId();
    } else if (backend.getBaseUrl() != null) {
      uri = backend.getBaseUrl();
    } else {
      log.error("No valid URI found for backend: {}", backend.getName());
      return null;
    }

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
