package io.github.dumijdev.belanova.gateway.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckConfig {
  private String healthPath;
  private int intervalSeconds;
  private int timeoutSeconds;
  private int healthyThreshold;
  private int unhealthyThreshold;

  @ElementCollection
  @CollectionTable(name = "healthcheck_expected_headers", joinColumns = @JoinColumn(name = "backend_id"))
  @MapKeyColumn(name = "header_key")
  @Column(name = "header_value")
  private Map<String, String> expectedHeaders;

  @ElementCollection
  @CollectionTable(name = "healthcheck_expected_status_codes", joinColumns = @JoinColumn(name = "backend_id"))
  @Column(name = "status_code")
  private List<Integer> expectedStatusCodes;

}
