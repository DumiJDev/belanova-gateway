package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BackendService {
    private final List<Backend> backends = new ArrayList<>();
    public List<Backend> findAll() {
        return new ArrayList<>(backends);
    }
    public void save(Backend backend) {
        backends.removeIf(b -> b.getId().equals(backend.getId()));
        backends.add(backend);
    }
    public void delete(String id) {
        backends.removeIf(b -> b.getId().equals(id));
    }

    public Backend findByNameContaining(String searchTerm) {
        return findAll().stream()
            .filter(backend -> searchTerm.equals(backend.getName()))
            .findAny()
            .orElse(null);
    }

    public Backend findById(String backendId) {
        return findAll()
            .stream()
            .filter(backend -> backendId.equals(backend.getId()))
            .findFirst().orElse( null);
    }
}
