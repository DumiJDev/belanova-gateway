package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.PluginInfo;

import java.util.List;
import java.util.Map;

public interface PluginService {
  List<PluginInfo> getAllPlugins();

  void updatePluginConfiguration(String name, Map<String, Object> config);

  void disablePlugin(String name);

  void enablePlugin(String name);
}
