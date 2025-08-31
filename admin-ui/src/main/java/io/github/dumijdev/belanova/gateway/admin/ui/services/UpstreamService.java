package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UpstreamService {
    private final List<Upstream> upstreams = new ArrayList<>();
    public List<Upstream> findAll() {
        return new ArrayList<>(upstreams);
    }
    public void save(Upstream upstream) {
        upstreams.removeIf(u -> u.getId().equals(upstream.getId()));
        upstreams.add(upstream);
    }
    public void delete(String id) {
        upstreams.removeIf(u -> u.getId().equals(id));
    }
}
