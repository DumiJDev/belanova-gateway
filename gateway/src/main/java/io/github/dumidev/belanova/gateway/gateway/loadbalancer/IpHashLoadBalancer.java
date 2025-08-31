package io.github.dumidev.belanova.gateway.gateway.loadbalancer;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import io.github.dumijdev.belanova.gateway.common.model.UpstreamHealthStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("ipHashLoadBalancer")
public class IpHashLoadBalancer implements LoadBalancer {
    @Override
    public Optional<Upstream> select(List<Upstream> upstreams, HttpServletRequest request) {
        List<Upstream> healthyUpstreams = upstreams.stream()
                .filter(u -> u.isEnabled() && u.getStatus() == UpstreamHealthStatus.HEALTHY)
                .toList();
        if (healthyUpstreams.isEmpty()) {
            return Optional.empty();
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getRemoteAddr() != null ? request.getRemoteHost() : "127.0.0.1";
        }
        int hash = Math.abs(ip.hashCode());
        int index = hash % healthyUpstreams.size();
        return Optional.of(healthyUpstreams.get(index));
    }
}
