package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.Consumer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConsumerService {

    // In-memory storage for demo purposes - in production, this would be a database
    private final Map<String, Consumer> consumers = new ConcurrentHashMap<>();

    public ConsumerService() {
        // Add some sample consumers for demo
        createSampleConsumers();
    }

    public List<Consumer> getAllConsumers() {
        return consumers.values().stream()
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
            .collect(Collectors.toList());
    }

    public Consumer getConsumerById(String id) {
        return consumers.get(id);
    }

    public Consumer createConsumer(String username, String customId) {
        if (consumers.values().stream().anyMatch(c -> c.username().equals(username))) {
            throw new IllegalArgumentException("Consumer with username '" + username + "' already exists");
        }

        Consumer consumer = Consumer.create(username, customId);
        consumers.put(consumer.id(), consumer);
        return consumer;
    }

    public Consumer updateConsumer(String id, String username, String customId) {
        Consumer existing = consumers.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Consumer not found");
        }

        // Check if username is already taken by another consumer
        if (!existing.username().equals(username) &&
            consumers.values().stream().anyMatch(c -> c.username().equals(username))) {
            throw new IllegalArgumentException("Consumer with username '" + username + "' already exists");
        }

        Consumer updated = existing.withUsername(username).withCustomId(customId);
        consumers.put(id, updated);
        return updated;
    }

    public void updateConsumerStatus(String id, String status) {
        Consumer existing = consumers.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Consumer not found");
        }

        Consumer updated = existing.withStatus(status);
        consumers.put(id, updated);
    }

    public void deleteConsumer(String id) {
        if (!consumers.containsKey(id)) {
            throw new IllegalArgumentException("Consumer not found");
        }
        consumers.remove(id);
    }

    public List<Consumer> searchConsumers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllConsumers();
        }

        String lowerQuery = query.toLowerCase();
        return consumers.values().stream()
            .filter(consumer ->
                consumer.username().toLowerCase().contains(lowerQuery) ||
                (consumer.customId() != null && consumer.customId().toLowerCase().contains(lowerQuery))
            )
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
            .collect(Collectors.toList());
    }

    public List<Consumer> getConsumersByStatus(String status) {
        return consumers.values().stream()
            .filter(consumer -> consumer.status().equals(status))
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
            .collect(Collectors.toList());
    }

    private void createSampleConsumers() {
        createConsumer("api-user-1", "custom-001");
        createConsumer("mobile-app", "custom-002");
        createConsumer("web-client", "custom-003");
        createConsumer("partner-system", "custom-004");
        createConsumer("test-consumer", "custom-005");

        // Update some statuses for demo
        consumers.values().stream()
            .filter(c -> c.username().equals("test-consumer"))
            .findFirst()
            .ifPresent(c -> updateConsumerStatus(c.id(), "inactive"));
    }
}