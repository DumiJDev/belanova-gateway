package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendHealthStatus;
import io.github.dumijdev.belanova.gateway.admin.ui.models.GatewayStats;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RequestMetrics;
import io.github.dumijdev.belanova.gateway.admin.ui.services.DashboardService;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

  private final DashboardService dashboardService;
  private final TranslationService translationService;
  private final Grid<BackendHealthStatus> healthGrid;
  private final ScheduledExecutorService scheduler;
  private final Tabs dashboardTabs;
  private final VerticalLayout contentArea;

  private GatewayStats currentStats;
  private List<RequestMetrics> currentMetrics;

  public DashboardView(DashboardService dashboardService, TranslationService translationService) {
    this.dashboardService = dashboardService;
    this.translationService = translationService;
    this.healthGrid = new Grid<>(BackendHealthStatus.class, false);
    this.scheduler = Executors.newScheduledThreadPool(1);
    this.dashboardTabs = new Tabs();
    this.contentArea = new VerticalLayout();

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    // Kong-inspired styling
    addClassName("kong-dashboard");

    configureTabs();
    add(createHeader(), dashboardTabs, contentArea);

    // Start real-time updates
    startRealTimeUpdates();

    // Default to overview tab
    showOverview();
  }

  private Component createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.addClassName("kong-header");

    H2 title = new H2(translationService.getTranslation("dashboard.title"));
    title.addClassName("kong-fade-in");

    Div realtimeContainer = new Div();
    realtimeContainer.addClassName("realtime-indicator");

    Span realtimeText = new Span(translationService.getTranslation("common.loading"));
    realtimeText.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);

    HorizontalLayout realtimeStatus = new HorizontalLayout(realtimeContainer, realtimeText);
    realtimeStatus.setAlignItems(FlexComponent.Alignment.CENTER);
    realtimeStatus.setSpacing(false);

    Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH));
    refreshButton.addClassName("kong-button");
    refreshButton.addClassName("secondary");
    refreshButton.addClickListener(e -> refreshAllData());

    Span lastUpdated = new Span(translationService.getTranslation("dashboard.last.updated") + ": " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    lastUpdated.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.SMALL);
    lastUpdated.setId("last-updated");

    HorizontalLayout rightSection = new HorizontalLayout(lastUpdated, refreshButton);
    rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
    rightSection.setSpacing(true);

    header.add(title, realtimeStatus, rightSection);
    return header;
  }

  private void configureTabs() {
    Tab overviewTab = new Tab(new Icon(VaadinIcon.DASHBOARD), new Span("Overview"));
    Tab analyticsTab = new Tab(new Icon(VaadinIcon.CHART), new Span("Analytics"));
    Tab performanceTab = new Tab(new Icon(VaadinIcon.TRENDING_UP), new Span("Performance"));
    Tab healthTab = new Tab(new Icon(VaadinIcon.HEART), new Span("Health"));

    dashboardTabs.add(overviewTab, analyticsTab, performanceTab, healthTab);
    dashboardTabs.addClassName("kong-dashboard-tabs");

    dashboardTabs.addSelectedChangeListener(event -> {
      Tab selectedTab = event.getSelectedTab();
      if (selectedTab == overviewTab) {
        showOverview();
      } else if (selectedTab == analyticsTab) {
        showAnalytics();
      } else if (selectedTab == performanceTab) {
        showPerformance();
      } else if (selectedTab == healthTab) {
        showHealth();
      }
    });
  }

  private void showOverview() {
    contentArea.removeAll();
    contentArea.add(createStatsCards(), createMetricsSection());
  }

  private void showAnalytics() {
    contentArea.removeAll();
    contentArea.add(createAnalyticsCharts());
  }

  private void showPerformance() {
    contentArea.removeAll();
    contentArea.add(createPerformanceCharts());
  }

  private void showHealth() {
    contentArea.removeAll();
    contentArea.add(createHealthSection());
  }

  private void startRealTimeUpdates() {
    scheduler.scheduleAtFixedRate(() -> {
      getUI().ifPresent(ui -> ui.access(() -> {
        refreshAllData();
        updateLastUpdatedTime();
      }));
    }, 30, 30, TimeUnit.SECONDS);
  }

  private void updateLastUpdatedTime() {
    Span lastUpdated = (Span) getChildren()
        .filter(component -> "last-updated".equals(component.getId().orElse("")))
        .findFirst().orElse(null);

    if (lastUpdated != null) {
      lastUpdated.setText(translationService.getTranslation("dashboard.last.updated") + ": " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
  }

  private void refreshAllData() {
    try {
      currentStats = dashboardService.getGatewayStats();
      currentMetrics = dashboardService.getRequestMetrics();
      healthGrid.setItems(dashboardService.getBackendHealthStatus());

      // Update UI components with new data
      getUI().ifPresent(ui -> ui.access(() -> {
        // This will trigger re-rendering of components that depend on the data
        updateStatsCards();
        updateMetricsSection();
      }));
    } catch (Exception e) {
      // Handle refresh errors gracefully
      System.err.println("Error refreshing dashboard data: " + e.getMessage());
    }
  }

  private Component createStatsCards() {
    HorizontalLayout statsLayout = new HorizontalLayout();
    statsLayout.setSizeFull();
    statsLayout.addClassNames(LumoUtility.Margin.MEDIUM, LumoUtility.Gap.MEDIUM);
    statsLayout.setId("stats-cards");

    currentStats = dashboardService.getGatewayStats();

    statsLayout.add(
        createStatsCard("Total Backends",
            String.valueOf(currentStats.totalBackends()),
            VaadinIcon.SERVER, "primary"),

        createStatsCard("Active Services",
            String.valueOf(currentStats.activeServices()),
            VaadinIcon.COGS, "success"),

        createStatsCard("Requests/Min",
            String.format("%.1f", currentStats.requestsPerMinute()),
            VaadinIcon.TRENDING_UP, "contrast"),

        createStatsCard("Avg Response Time",
            String.format("%.2f ms", currentStats.averageResponseTime()),
            VaadinIcon.TIMER, "warning")
    );

    return statsLayout;
  }

  private void updateStatsCards() {
    if (currentStats != null) {
      // Find and update the stats layout
      getChildren().filter(component -> "stats-cards".equals(component.getId().orElse("")))
          .findFirst().ifPresent(component -> {
            if (component instanceof HorizontalLayout) {
              HorizontalLayout statsLayout = (HorizontalLayout) component;
              statsLayout.removeAll();
              statsLayout.add(
                  createStatsCard("Total Backends",
                      String.valueOf(currentStats.totalBackends()),
                      VaadinIcon.SERVER, "primary"),

                  createStatsCard("Active Services",
                      String.valueOf(currentStats.activeServices()),
                      VaadinIcon.COGS, "success"),

                  createStatsCard("Requests/Min",
                      String.format("%.1f", currentStats.requestsPerMinute()),
                      VaadinIcon.TRENDING_UP, "contrast"),

                  createStatsCard("Avg Response Time",
                      String.format("%.2f ms", currentStats.averageResponseTime()),
                      VaadinIcon.TIMER, "warning")
              );
            }
          });
    }
  }

  private Component createStatsCard(String title, String value, VaadinIcon iconType, String theme) {
    Icon icon = iconType.create();
    icon.addClassName("card-icon");

    Span titleSpan = new Span(title);
    titleSpan.addClassName("card-title");

    Span valueSpan = new Span(value);
    valueSpan.addClassName("card-value");

    HorizontalLayout header = new HorizontalLayout(icon);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

    VerticalLayout content = new VerticalLayout(header, titleSpan, valueSpan);
    content.addClassName("kong-stats-card");
    content.setSpacing(false);
    content.setFlexGrow(1);

    return content;
  }

  private Component createMetricsSection() {
    HorizontalLayout metricsLayout = new HorizontalLayout();
    metricsLayout.setSizeFull();
    metricsLayout.addClassNames(LumoUtility.Margin.MEDIUM, LumoUtility.Gap.MEDIUM);
    metricsLayout.setId("metrics-section");

    currentMetrics = dashboardService.getRequestMetrics();

    // Enhanced metrics visualization with Kong-inspired design
    Component requestMetrics = createRequestMetricsCard();
    Component responseTimeMetrics = createResponseTimeMetricsCard();
    Component throughputMetrics = createThroughputMetricsCard();

    metricsLayout.add(requestMetrics, responseTimeMetrics, throughputMetrics);
    return metricsLayout;
  }

  private void updateMetricsSection() {
    if (currentMetrics != null) {
      getChildren().filter(component -> "metrics-section".equals(component.getId().orElse("")))
          .findFirst().ifPresent(component -> {
            if (component instanceof HorizontalLayout) {
              HorizontalLayout metricsLayout = (HorizontalLayout) component;
              metricsLayout.removeAll();
              metricsLayout.add(
                  createRequestMetricsCard(),
                  createResponseTimeMetricsCard(),
                  createThroughputMetricsCard()
              );
            }
          });
    }
  }

  private Component createRequestMetricsCard() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3("Request Metrics (Last 24h)");

    if (currentMetrics != null && !currentMetrics.isEmpty()) {
      RequestMetrics latest = currentMetrics.getLast();

      Span currentRequests = new Span(latest.requestCount() + " requests");
      currentRequests.addClassNames(LumoUtility.FontSize.LARGE);

      // Kong-inspired progress bar
      Div progressContainer = new Div();
      progressContainer.addClassName("kong-progress-bar");

      Div progressFill = new Div();
      progressFill.addClassName("progress-fill");
      progressFill.getStyle().set("width", Math.min((latest.requestCount() / 1000.0) * 100, 100) + "%");

      progressContainer.add(progressFill);

      Span loadLabel = new Span("System Load");

      card.add(title, currentRequests, loadLabel, progressContainer);
    } else {
      Span noData = new Span("No metrics available");
      card.add(title, noData);
    }

    return card;
  }

  private Component createResponseTimeMetricsCard() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3("Response Time Metrics");

    if (currentMetrics != null && !currentMetrics.isEmpty()) {
      double avgResponseTime = currentMetrics.stream()
          .mapToDouble(RequestMetrics::averageResponseTime)
          .average()
          .orElse(0.0);

      double maxResponseTime = currentMetrics.stream()
          .mapToDouble(RequestMetrics::averageResponseTime)
          .max()
          .orElse(0.0);

      Span avgSpan = new Span(String.format("%.1f ms", avgResponseTime));
      avgSpan.addClassNames(LumoUtility.FontSize.LARGE);

      Span avgLabel = new Span("Average");

      Span maxSpan = new Span(String.format("%.1f ms", maxResponseTime));
      maxSpan.addClassNames(LumoUtility.FontSize.MEDIUM);

      Span maxLabel = new Span("Peak");

      // Performance indicator with Kong styling
      Div progressContainer = new Div();
      progressContainer.addClassName("kong-progress-bar");

      Div progressFill = new Div();
      progressFill.addClassName("progress-fill");
      progressFill.getStyle().set("width", Math.min((avgResponseTime / 1000.0) * 100, 100) + "%");

      progressContainer.add(progressFill);

      String perfStatus = avgResponseTime < 100 ? "Excellent" :
          avgResponseTime < 300 ? "Good" :
              avgResponseTime < 500 ? "Fair" : "Poor";

      Span perfLabel = new Span("Performance: " + perfStatus);

      VerticalLayout avgLayout = new VerticalLayout(avgLabel, avgSpan);
      avgLayout.setPadding(false);
      avgLayout.setSpacing(false);

      VerticalLayout maxLayout = new VerticalLayout(maxLabel, maxSpan);
      maxLayout.setPadding(false);
      maxLayout.setSpacing(false);

      HorizontalLayout valuesLayout = new HorizontalLayout(avgLayout, maxLayout);
      valuesLayout.setWidthFull();

      card.add(title, valuesLayout, perfLabel, progressContainer);
    } else {
      Span noDataSpan = new Span("No metrics available");
      card.add(title, noDataSpan);
    }

    return card;
  }

  private Component createThroughputMetricsCard() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3("Throughput Metrics");

    if (currentMetrics != null && !currentMetrics.isEmpty()) {
      RequestMetrics latest = currentMetrics.getLast();

      // Calculate throughput metrics
      double requestsPerSecond = latest.requestCount() / 60.0; // Assuming 1 minute window
      double successRate = 95.0 + Math.random() * 5.0; // Mock success rate

      Span throughputValue = new Span(String.format("%.1f req/s", requestsPerSecond));
      throughputValue.addClassNames(LumoUtility.FontSize.LARGE);

      Span successRateValue = new Span(String.format("%.1f%% success", successRate));
      successRateValue.addClassNames(LumoUtility.FontSize.MEDIUM);

      // Throughput indicator
      Div progressContainer = new Div();
      progressContainer.addClassName("kong-progress-bar");

      Div progressFill = new Div();
      progressFill.addClassName("progress-fill");
      progressFill.getStyle().set("width", Math.min(requestsPerSecond * 10, 100) + "%");

      progressContainer.add(progressFill);

      card.add(title, throughputValue, successRateValue, progressContainer);
    } else {
      Span noDataSpan = new Span("No throughput data available");
      card.add(title, noDataSpan);
    }

    return card;
  }

  private Component createHealthSection() {
    VerticalLayout section = new VerticalLayout();
    section.addClassNames(LumoUtility.Margin.MEDIUM);

    H3 title = new H3("Backend Health Status");
    title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

    configureHealthGrid();
    healthGrid.addClassName("kong-grid");
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
    badge.addClassName("kong-badge");

    switch (status.status().toLowerCase()) {
      case "healthy":
        badge.addClassName("success");
        break;
      case "unhealthy":
        badge.addClassName("error");
        break;
      case "warning":
        badge.addClassName("warning");
        break;
      default:
        badge.addClassName("info");
    }

    return badge;
  }

  private Component createAnalyticsCharts() {
    VerticalLayout analyticsLayout = new VerticalLayout();
    analyticsLayout.setSizeFull();
    analyticsLayout.setPadding(false);
    analyticsLayout.setSpacing(true);

    HorizontalLayout chartsRow1 = new HorizontalLayout();
    chartsRow1.setSizeFull();
    chartsRow1.setSpacing(true);

    chartsRow1.add(createRequestTrendChart(), createResponseTimeChart());

    HorizontalLayout chartsRow2 = new HorizontalLayout();
    chartsRow2.setSizeFull();
    chartsRow2.setSpacing(true);

    chartsRow2.add(createErrorRateChart(), createThroughputChart());

    analyticsLayout.add(chartsRow1, chartsRow2);
    return analyticsLayout;
  }

  private Component createPerformanceCharts() {
    VerticalLayout performanceLayout = new VerticalLayout();
    performanceLayout.setSizeFull();
    performanceLayout.setPadding(false);
    performanceLayout.setSpacing(true);

    HorizontalLayout chartsRow1 = new HorizontalLayout();
    chartsRow1.setSizeFull();
    chartsRow1.setSpacing(true);

    chartsRow1.add(createLatencyDistributionChart(), createBackendPerformanceChart());

    HorizontalLayout chartsRow2 = new HorizontalLayout();
    chartsRow2.setSizeFull();
    chartsRow2.setSpacing(true);

    chartsRow2.add(createMemoryUsageChart(), createCpuUsageChart());

    performanceLayout.add(chartsRow1, chartsRow2);
    return performanceLayout;
  }

  private Component createRequestTrendChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.requests.trend"));
    Div chartContainer = createSimpleLineChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createResponseTimeChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.response.time.trend"));
    Div chartContainer = createSimpleAreaChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createErrorRateChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.error.rates"));
    Div chartContainer = createSimpleBarChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createThroughputChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.throughput"));
    Div chartContainer = createSimpleColumnChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createLatencyDistributionChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.latency.distribution"));
    Div chartContainer = createSimplePieChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createBackendPerformanceChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.backend.performance"));
    Div chartContainer = createSimpleScatterChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createMemoryUsageChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.memory.usage"));
    Div chartContainer = createSimpleAreaChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createCpuUsageChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H3 title = new H3(translationService.getTranslation("dashboard.cpu.usage"));
    Div chartContainer = createSimpleLineChart();
    chartContainer.setHeight("300px");

    card.add(title, chartContainer);
    return card;
  }

  // Open source chart alternatives using CSS and HTML
  private Div createSimpleLineChart() {
    Div chart = new Div();
    chart.addClassName("kong-simple-chart");
    chart.addClassName("kong-line-chart");

    // Create sample data visualization
    for (int i = 0; i < 12; i++) {
      Div bar = new Div();
      bar.addClassName("kong-chart-bar");
      int height = 20 + (int)(Math.random() * 80); // Random height between 20-100
      bar.getStyle().set("height", height + "%");
      bar.getStyle().set("animation-delay", (i * 0.1) + "s");
      chart.add(bar);
    }

    return chart;
  }

  private Div createSimpleAreaChart() {
    Div chart = new Div();
    chart.addClassName("kong-simple-chart");
    chart.addClassName("kong-area-chart");

    // Create area chart visualization
    Div area = new Div();
    area.addClassName("kong-chart-area");
    String points = "0,100 8.33,80 16.66,85 25,70 33.33,75 41.66,60 50,65 58.33,55 66.66,70 75,50 83.33,60 91.66,45 100,55";
    area.getStyle().set("clip-path", "polygon(" + points + ")");
    chart.add(area);

    return chart;
  }

  private Div createSimpleBarChart() {
    Div chart = new Div();
    chart.addClassName("kong-simple-chart");
    chart.addClassName("kong-bar-chart");

    String[] labels = {"API-1", "API-2", "API-3", "API-4", "API-5", "API-6"};
    for (int i = 0; i < labels.length; i++) {
      Div bar = new Div();
      bar.addClassName("kong-chart-bar");
      int height = 15 + (int)(Math.random() * 70);
      bar.getStyle().set("height", height + "%");
      bar.getStyle().set("animation-delay", (i * 0.15) + "s");

      Span label = new Span(labels[i]);
      label.addClassName("kong-chart-label");
      bar.add(label);

      chart.add(bar);
    }

    return chart;
  }

  private Div createSimpleColumnChart() {
    Div chart = new Div();
    chart.addClassName("kong-simple-chart");
    chart.addClassName("kong-column-chart");

    String[] services = {"User", "Order", "Payment", "Auth", "Search"};
    for (int i = 0; i < services.length; i++) {
      Div column = new Div();
      column.addClassName("kong-chart-column");
      int height = 25 + (int)(Math.random() * 60);
      column.getStyle().set("height", height + "%");
      column.getStyle().set("animation-delay", (i * 0.2) + "s");

      Span label = new Span(services[i]);
      label.addClassName("kong-chart-label");
      column.add(label);

      chart.add(column);
    }

    return chart;
  }

  private Div createSimplePieChart() {
    Div chart = new Div();
    chart.addClassName("kong-simple-chart");
    chart.addClassName("kong-pie-chart");

    // Create pie segments
    String[] segments = {"#667eea", "#764ba2", "#f093fb", "#4facfe", "#00f2fe"};
    int[] percentages = {35, 28, 22, 12, 3};
    int currentAngle = 0;

    for (int i = 0; i < segments.length; i++) {
      Div segment = new Div();
      segment.addClassName("kong-pie-segment");
      segment.getStyle().set("background", "conic-gradient(" + segments[i] + " " + currentAngle + "deg " + (currentAngle + (percentages[i] * 3.6)) + "deg, transparent " + (currentAngle + (percentages[i] * 3.6)) + "deg)");
      currentAngle += percentages[i] * 3.6;
      chart.add(segment);
    }

    return chart;
  }

  private Div createSimpleScatterChart() {
    Div chart = new Div();
    chart.addClassName("kong-simple-chart");
    chart.addClassName("kong-scatter-chart");

    // Create scatter points
    for (int i = 0; i < 20; i++) {
      Div point = new Div();
      point.addClassName("kong-scatter-point");
      int x = 10 + (int)(Math.random() * 80);
      int y = 10 + (int)(Math.random() * 80);
      point.getStyle().set("left", x + "%");
      point.getStyle().set("bottom", y + "%");
      point.getStyle().set("animation-delay", (i * 0.1) + "s");
      chart.add(point);
    }

    return chart;
  }
}