package io.github.dumidev.belanova.gateway.gateway.loadbalancer;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

public interface LoadBalancer {
    Optional<Upstream> select(List<Upstream> upstreams, HttpServletRequest request);
}
