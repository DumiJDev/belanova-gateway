package io.github.dumijdev.belanova.gateway.common.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Backend {
  @Id
  private String id;

  public String getId() {
    return id;
  }
  private String name;
  private String description;
  private String baseUrl;
  private String serviceId;
  private String generalPath;
  private String status;
  private boolean enabled;
  private boolean useServiceDiscovery;

  @Embedded
  private HealthCheckConfig healthCheck;

  @OneToMany(mappedBy = "backend", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<Service> services;

  @OneToMany(mappedBy = "backend", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<Upstream> upstreams;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Backend backend = (Backend) o;
    return getId() != null && Objects.equals(getId(), backend.getId());
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode();
  }
}
