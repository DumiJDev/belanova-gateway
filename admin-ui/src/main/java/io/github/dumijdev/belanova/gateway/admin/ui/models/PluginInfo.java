package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.util.Map;

public record PluginInfo(
    String name,
    Phase phase,
    String description,
    String order,
    Map<String, String> configuration,
    boolean enabled
)
{
  public enum Phase {
    PRE_AUTH, AUTH, POST_AUTH, PRE_REQUEST, REQUEST, POST_REQUEST, ERROR,
  }
}
