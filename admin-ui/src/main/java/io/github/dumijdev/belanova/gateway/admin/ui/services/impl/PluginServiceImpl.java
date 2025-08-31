package io.github.dumijdev.belanova.gateway.admin.ui.services.impl;

import io.github.dumijdev.belanova.gateway.admin.ui.models.PluginInfo;
import io.github.dumijdev.belanova.gateway.admin.ui.services.PluginService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PluginServiceImpl implements PluginService {
  @Override
  public List<PluginInfo> getAllPlugins() {
    return List.of();
  }

  @Override
  public void updatePluginConfiguration(String name, Map<String, Object> config) {

  }

  @Override
  public void disablePlugin(String name) {

  }

  @Override
  public void enablePlugin(String name) {

  }
}
