package io.github.dumijdev.belanova.gateway.admin.ui.services;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceService {
    private final List<io.github.dumijdev.belanova.gateway.common.model.Service> services = new ArrayList<>();
    public List<io.github.dumijdev.belanova.gateway.common.model.Service> findAll() {
        return new ArrayList<>(services);
    }
    public void save(io.github.dumijdev.belanova.gateway.common.model.Service service) {
        services.removeIf(s -> s.getId().equals(service.getId()));
        services.add(service);
    }
    public void delete(String id) {
        services.removeIf(s -> s.getId().equals(id));
    }
}
