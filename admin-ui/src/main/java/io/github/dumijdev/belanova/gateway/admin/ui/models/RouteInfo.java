package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.util.List;
import java.util.Map;

public record RouteInfo(
    String routeId,
    String path,
    String status,
    String targetUri,
    Map<String, String> metadata,
    List<String> filters,
    List<String> methods
) {

}
