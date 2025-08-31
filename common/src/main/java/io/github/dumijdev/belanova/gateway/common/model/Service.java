package io.github.dumijdev.belanova.gateway.common.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Service {
  @Id
  private String id;

  public String getId() {
    return id;
  }
  private String name;
  private String path;

  @ManyToOne
  @JoinColumn(name = "backend_id")
  private Backend backend;

  @ElementCollection
  @CollectionTable(name = "service_methods", joinColumns = @JoinColumn(name = "service_id"))
  @Column(name = "method")
  private List<String> methods;

  private boolean enabled;

  @ElementCollection
  @CollectionTable(name = "service_metadata", joinColumns = @JoinColumn(name = "service_id"))
  @MapKeyColumn(name = "metadata_key")
  @Column(name = "metadata_value")
  private Map<String, String> metadata;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Service service = (Service) o;
    return getId() != null && Objects.equals(getId(), service.getId());
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode();
  }
}
