package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendHealthStatus;
import io.github.dumijdev.belanova.gateway.admin.ui.models.GatewayStats;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RequestMetrics;
import io.github.dumijdev.belanova.gateway.admin.ui.services.DashboardService;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

  private final DashboardService dashboardService;
  private final Grid<BackendHealthStatus> healthGrid;

  public DashboardView(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
    this.healthGrid = new Grid<>(BackendHealthStatus.class, false);

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    add(createStatsCards(), createMetricsSection(), createHealthSection());
  }

  private Component createStatsCards() {
    HorizontalLayout statsLayout = new HorizontalLayout();
    statsLayout.setSizeFull();
    statsLayout.addClassNames(LumoUtility.Margin.MEDIUM, LumoUtility.Gap.MEDIUM);

    GatewayStats stats = dashboardService.getGatewayStats();

    statsLayout.add(
        createStatsCard("Total Backends",
            String.valueOf(stats.totalBackends()),
            VaadinIcon.SERVER, "primary"),

        createStatsCard("Active Services",
            String.valueOf(stats.activeServices()),
            VaadinIcon.COGS, "success"),

        createStatsCard("Requests/Min",
            String.format("%.1f", stats.requestsPerMinute()),
            VaadinIcon.TRENDING_UP, "contrast"),

        createStatsCard("Avg Response Time",
            String.format("%.2f ms", stats.averageResponseTime()),
            VaadinIcon.TIMER, "warning")
    );

    return statsLayout;
  }

  private Component createStatsCard(String title, String value, VaadinIcon iconType, String theme) {
    Icon icon = iconType.create();
    icon.addClassNames(
        LumoUtility.IconSize.LARGE,
        LumoUtility.TextColor.PRIMARY
    );

    Span titleSpan = new Span(title);
    titleSpan.addClassNames(
        LumoUtility.FontSize.SMALL,
        LumoUtility.TextColor.SECONDARY,
        LumoUtility.FontWeight.MEDIUM
    );

    Span valueSpan = new Span(value);
    valueSpan.addClassNames(
        LumoUtility.FontSize.XXXLARGE,
        LumoUtility.FontWeight.BOLD
    );

    HorizontalLayout header = new HorizontalLayout(icon);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

    VerticalLayout content = new VerticalLayout(header, titleSpan, valueSpan);
    content.addClassNames(
        LumoUtility.Padding.LARGE,
        LumoUtility.Background.BASE,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.Border.ALL
    );
    content.setSpacing(false);
    content.setFlexGrow(1);

    return content;
  }

  private Component createMetricsSection() {
    HorizontalLayout metricsLayout = new HorizontalLayout();
    metricsLayout.setSizeFull();
    metricsLayout.addClassNames(LumoUtility.Margin.MEDIUM, LumoUtility.Gap.MEDIUM);

    // Simple metrics visualization using progress bars and text
    Component requestMetrics = createRequestMetricsCard();
    Component responseTimeMetrics = createResponseTimeMetricsCard();

    metricsLayout.add(requestMetrics, responseTimeMetrics);
    return metricsLayout;
  }

  private Component createRequestMetricsCard() {
    VerticalLayout card = new VerticalLayout();
    card.addClassNames(
        LumoUtility.Padding.LARGE,
        LumoUtility.Background.BASE,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.Border.ALL
    );
    card.setFlexGrow(1);

    H3 title = new H3("Request Metrics (Last 24h)");
    title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

    List<RequestMetrics> metrics = dashboardService.getRequestMetrics();

    if (!metrics.isEmpty()) {
      RequestMetrics latest = metrics.getLast();

      Span currentRequests = new Span("Current: " + latest.requestCount() + " requests");
      currentRequests.addClassNames(LumoUtility.FontSize.LARGE);

      // Simple progress bar showing relative load
      ProgressBar loadBar = new ProgressBar();
      loadBar.setMin(0);
      loadBar.setMax(1000); // Assuming max 1000 requests as reference
      loadBar.setValue(Math.min(latest.requestCount(), 1000));
      loadBar.setWidthFull();

      Span loadLabel = new Span("System Load");
      loadLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

      card.add(title, currentRequests, loadLabel, loadBar);
    } else {
      card.add(title, new Span("No metrics available"));
    }

    return card;
  }

  private Component createResponseTimeMetricsCard() {
    VerticalLayout card = new VerticalLayout();
    card.addClassNames(
        LumoUtility.Padding.LARGE,
        LumoUtility.Background.BASE,
        LumoUtility.BorderRadius.MEDIUM,
        LumoUtility.Border.ALL
    );
    card.setFlexGrow(1);

    H3 title = new H3("Response Time Metrics");
    title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

    List<RequestMetrics> metrics = dashboardService.getRequestMetrics();

    if (!metrics.isEmpty()) {
      double avgResponseTime = metrics.stream()
          .mapToDouble(RequestMetrics::averageResponseTime)
          .average()
          .orElse(0.0);

      double maxResponseTime = metrics.stream()
          .mapToDouble(RequestMetrics::averageResponseTime)
          .max()
          .orElse(0.0);

      Span avgSpan = new Span("Average: " + String.format("%.2f ms", avgResponseTime));
      avgSpan.addClassNames(LumoUtility.FontSize.LARGE);

      Span maxSpan = new Span("Peak: " + String.format("%.2f ms", maxResponseTime));
      maxSpan.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.SECONDARY);

      // Performance indicator
      ProgressBar perfBar = new ProgressBar();
      perfBar.setMin(0);
      perfBar.setMax(1000); // Assuming 1000ms as max acceptable response time
      perfBar.setValue(Math.min(avgResponseTime, 1000));
      perfBar.setWidthFull();

      String perfStatus = avgResponseTime < 100 ? "Excellent" :
          avgResponseTime < 300 ? "Good" :
              avgResponseTime < 500 ? "Fair" : "Poor";

      Span perfLabel = new Span("Performance: " + perfStatus);
      perfLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

      card.add(title, avgSpan, maxSpan, perfLabel, perfBar);
    } else {
      card.add(title, new Span("No metrics available"));
    }

    return card;
  }

  private Component createHealthSection() {
    VerticalLayout section = new VerticalLayout();
    section.addClassNames(LumoUtility.Margin.MEDIUM);

    H3 title = new H3("Backend Health Status");
    title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

    configureHealthGrid();
    healthGrid.setItems(dashboardService.getBackendHealthStatus());

    section.add(title, healthGrid);
    return section;
  }

  private void configureHealthGrid() {
    healthGrid.addColumn(BackendHealthStatus::name)
        .setHeader("Backend Name")
        .setSortable(true);

    healthGrid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader("Status")
        .setWidth("120px")
        .setFlexGrow(0);

    healthGrid.addColumn(backendHealth -> backendHealth.responseTime() + " ms")
        .setHeader("Response Time")
        .setSortable(true);

    healthGrid.addColumn(BackendHealthStatus::healthyUpstreams)
        .setHeader("Healthy Upstreams")
        .setSortable(true);

    healthGrid.addColumn(backendHealth ->
            backendHealth.lastChecked().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        .setHeader("Last Check")
        .setSortable(true);

  }

  private Component createStatusBadge(BackendHealthStatus status) {
    Span badge = new Span(status.status());
    badge.getElement().getThemeList().add("badge");

    switch (status.status().toLowerCase()) {
      case "healthy":
        badge.getElement().getThemeList().add("success");
        break;
      case "unhealthy":
        badge.getElement().getThemeList().add("error");
        break;
      case "warning":
        badge.getElement().getThemeList().add("warning");
        break;
      default:
        badge.getElement().getThemeList().add("contrast");
    }

    return badge;
  }
}