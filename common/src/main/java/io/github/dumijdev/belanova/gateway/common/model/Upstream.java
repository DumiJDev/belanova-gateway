package io.github.dumijdev.belanova.gateway.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Upstream {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "backend_id")
    private Backend backend;

    private String host;
    private int port;
    private int weight;
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private UpstreamHealthStatus status;
}
