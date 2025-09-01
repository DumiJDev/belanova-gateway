package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendHealthStatus;
import io.github.dumijdev.belanova.gateway.admin.ui.models.LogEntry;
import io.github.dumijdev.belanova.gateway.admin.ui.models.MetricInfo;
import io.github.dumijdev.belanova.gateway.admin.ui.services.MonitoringService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Monitoring")
@Route(value = "monitoring", layout = MainLayout.class)
public class MonitoringView extends VerticalLayout {

  private final MonitoringService monitoringService;
  private final TranslationService translationService;
  private final Tabs tabSheet;
  private final VerticalLayout contentArea;

  // Health Check Tab
  private final Grid<BackendHealthStatus> healthGrid;

  // Metrics Tab
  private final Grid<MetricInfo> metricsGrid;

  // Logs Tab
  private final Grid<LogEntry> logsGrid;
  private final TextField logSearchField;
  private final Select<String> logLevelFilter;

  public MonitoringView(MonitoringService monitoringService, TranslationService translationService) {
    this.monitoringService = monitoringService;
    this.translationService = translationService;
    this.tabSheet = new Tabs();
    this.contentArea = new VerticalLayout();
    this.healthGrid = new Grid<>(BackendHealthStatus.class, false);
    this.metricsGrid = new Grid<>(MetricInfo.class, false);
    this.logsGrid = new Grid<>(LogEntry.class, false);
    this.logSearchField = new TextField();
    this.logLevelFilter = new Select<>();

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    // Kong-inspired styling
    getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");

    configureTabs();
    configureGrids();

    add(createHeader(), tabSheet, contentArea);

    // Default to overview tab
    showOverview();
  }

  private Component createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM);

    H3 title = new H3("System Monitoring");
    title.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontWeight.BOLD);

    Button refreshAllButton = new Button("Refresh All", new Icon(VaadinIcon.REFRESH));
    refreshAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshAllButton.getElement().setAttribute("kong-primary", "");
    refreshAllButton.addClickListener(e -> refreshAllData());

    header.add(title, refreshAllButton);
    return header;
  }

  private void configureTabs() {
    Tab overviewTab = new Tab(new Icon(VaadinIcon.DASHBOARD), new Span("Overview"));
    Tab healthTab = new Tab(new Icon(VaadinIcon.HEART), new Span("Health Checks"));
    Tab metricsTab = new Tab(new Icon(VaadinIcon.BAR_CHART), new Span("Metrics"));
    Tab performanceTab = new Tab(new Icon(VaadinIcon.TRENDING_UP), new Span("Performance"));
    Tab logsTab = new Tab(new Icon(VaadinIcon.FILE_TEXT), new Span("Logs"));

    tabSheet.add(overviewTab, healthTab, metricsTab, performanceTab, logsTab);
    tabSheet.addClassName("kong-dashboard-tabs");

    tabSheet.addSelectedChangeListener(event -> {
      Tab selectedTab = event.getSelectedTab();
      if (selectedTab == overviewTab) {
        showOverview();
      } else if (selectedTab == healthTab) {
        showHealthCheck();
      } else if (selectedTab == metricsTab) {
        showMetrics();
      } else if (selectedTab == performanceTab) {
        showPerformance();
      } else if (selectedTab == logsTab) {
        showLogs();
      }
    });
  }

  private void showOverview() {
    contentArea.removeAll();
    contentArea.add(createMonitoringOverview());
  }

  private void refreshAllData() {
    updateHealthStatus();
    updateMetrics();
    updateLogs();
    showNotification("All monitoring data refreshed", NotificationVariant.LUMO_SUCCESS);
  }

  private void configureGrids() {
    configureHealthGrid();
    configureMetricsGrid();
    configureLogsGrid();
  }

  private void configureHealthGrid() {
    healthGrid.setSizeFull();
    healthGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    healthGrid.addColumn(BackendHealthStatus::name)
        .setHeader("Backend")
        .setSortable(true)
        .setFlexGrow(1);

    healthGrid.addColumn(new ComponentRenderer<>(this::createHealthStatusBadge))
        .setHeader("Status")
        .setWidth("120px");

    healthGrid.addColumn(health -> health.responseTime() + " ms")
        .setHeader("Response Time")
        .setSortable(true)
        .setWidth("130px");

    healthGrid.addColumn(BackendHealthStatus::healthyUpstreams)
        .setHeader("Healthy/Total")
        .setSortable(true)
        .setWidth("120px");

    healthGrid.addColumn(health ->
            health.lastChecked().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm:ss")))
        .setHeader("Last Checked")
        .setSortable(true)
        .setFlexGrow(1);

    healthGrid.addColumn(new ComponentRenderer<>(this::createHealthActions))
        .setHeader("Actions")
        .setWidth("100px")
        .setFlexGrow(0);

    healthGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createHealthDetails));
  }

  private void configureMetricsGrid() {
    metricsGrid.setSizeFull();
    metricsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    metricsGrid.addColumn(MetricInfo::name)
        .setHeader("Metric Name")
        .setSortable(true)
        .setFlexGrow(1);

    metricsGrid.addColumn(MetricInfo::value)
        .setHeader("Current Value")
        .setSortable(true)
        .setWidth("150px");

    metricsGrid.addColumn(MetricInfo::unit)
        .setHeader("Unit")
        .setWidth("80px");

    metricsGrid.addColumn(MetricInfo::description)
        .setHeader("Description")
        .setFlexGrow(2);

    metricsGrid.addColumn(metric ->
            metric.lastUpdated().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        .setHeader("Last Updated")
        .setWidth("100px");
  }

  private void configureLogsGrid() {
    logsGrid.setSizeFull();
    logsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    logsGrid.addColumn(log ->
            log.timestamp().format(DateTimeFormatter.ofPattern("MMM dd HH:mm:ss")))
        .setHeader("Timestamp")
        .setSortable(true)
        .setWidth("150px");

    logsGrid.addColumn(new ComponentRenderer<>(this::createLogLevelBadge))
        .setHeader("Level")
        .setWidth("80px");

    logsGrid.addColumn(LogEntry::logger)
        .setHeader("Logger")
        .setFlexGrow(1);

    logsGrid.addColumn(LogEntry::message)
        .setHeader("Message")
        .setFlexGrow(3);

    logsGrid.setItemDetailsRenderer(new ComponentRenderer<>(this::createLogDetails));

    // Configure log filters
    logLevelFilter.setLabel("Log Level");
    logLevelFilter.setItems("All", "ERROR", "WARN", "INFO", "DEBUG");
    logLevelFilter.setValue("All");
    logLevelFilter.addValueChangeListener(e -> updateLogs());

    logSearchField.setPlaceholder("Search logs...");
    logSearchField.setClearButtonVisible(true);
    logSearchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
    logSearchField.addValueChangeListener(e -> updateLogs());
  }

  private void showHealthCheck() {
    contentArea.removeAll();

    Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshButton.addClickListener(e -> updateHealthStatus());

    HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
    toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

    contentArea.add(toolbar, healthGrid);
    updateHealthStatus();
  }

  private void showMetrics() {
    contentArea.removeAll();

    Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshButton.addClickListener(e -> updateMetrics());

    HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
    toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

    contentArea.add(toolbar, metricsGrid);
    updateMetrics();
  }

  private void showLogs() {
    contentArea.removeAll();

    Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    Button clearButton = new Button("Clear", new Icon(VaadinIcon.TRASH));
    clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    clearButton.addClickListener(e -> clearLogs());

    HorizontalLayout toolbar = new HorizontalLayout(logLevelFilter, logSearchField, refreshButton, clearButton);
    toolbar.setAlignItems(FlexComponent.Alignment.END);

    contentArea.add(toolbar, logsGrid);
    updateLogs();
  }

  private Component createHealthStatusBadge(BackendHealthStatus health) {
    Span badge = new Span(health.status());
    badge.getElement().getThemeList().add("badge");

    switch (health.status().toLowerCase()) {
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

  private Component createHealthActions(BackendHealthStatus health) {
    Button checkButton = new Button(new Icon(VaadinIcon.PLAY));
    checkButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    checkButton.setTooltipText("Force Health Check");
    checkButton.addClickListener(e -> forceHealthCheck(health.backendId()));

    return checkButton;
  }

  private Component createHealthDetails(BackendHealthStatus health) {
    VerticalLayout details = new VerticalLayout();
    details.setPadding(true);
    details.addClassNames(LumoUtility.Background.CONTRAST_5);

    // Health check configuration
    H4 configTitle = new H4("Health Check Configuration");
    Span healthPath = new Span("Health Path: /health");
    Span interval = new Span("Check Interval: 30s");
    Span timeout = new Span("Timeout: 10s");

    VerticalLayout configLayout = new VerticalLayout(healthPath, interval, timeout);
    configLayout.setPadding(false);
    configLayout.setSpacing(false);

    details.add(configTitle, configLayout);
    return details;
  }

    private Component createLogLevelBadge (LogEntry log){
      Span badge = new Span(log.level());
      badge.getElement().getThemeList().add("badge");

      switch (log.level().toUpperCase()) {
        case "ERROR":
          badge.getElement().getThemeList().add("error");
          break;
        case "WARN":
          badge.getElement().getThemeList().add("warning");
          break;
        case "INFO":
          badge.getElement().getThemeList().add("success");
          break;
        case "DEBUG":
          badge.getElement().getThemeList().add("contrast");
          break;
        default:
          badge.getElement().getThemeList().add("primary");
      }

      return badge;
    }

    private Component createLogDetails (LogEntry log){
      VerticalLayout details = new VerticalLayout();
      details.setPadding(true);
      details.addClassNames(LumoUtility.Background.CONTRAST_5);

      if (log.stackTrace() != null && !log.stackTrace().isEmpty()) {
        H4 stackTitle = new H4("Stack Trace");
        Pre stackPre = new Pre(log.stackTrace());
        stackPre.addClassNames(LumoUtility.FontSize.SMALL);
        stackPre.getStyle().set("max-height", "300px");
        stackPre.getStyle().set("overflow", "auto");
        details.add(stackTitle, stackPre);
      }

      if (log.requestId() != null) {
        Span requestIdSpan = new Span("Request ID: " + log.requestId());
        requestIdSpan.addClassNames(LumoUtility.FontSize.SMALL);
        details.add(requestIdSpan);
      }

      return details;
    }

    private void updateHealthStatus () {
      try {
        List<BackendHealthStatus> healthStatuses = monitoringService.getBackendHealthStatuses();
        healthGrid.setItems(healthStatuses);
      } catch (Exception e) {
        showNotification("Error loading health status: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    }

    private void updateMetrics () {
      try {
        List<MetricInfo> metrics = monitoringService.getSystemMetrics();
        metricsGrid.setItems(metrics);
      } catch (Exception e) {
        showNotification("Error loading metrics: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    }

    private void updateLogs () {
      try {
        String searchTerm = logSearchField.getValue();
        String logLevel = logLevelFilter.getValue();

        List<LogEntry> logs = monitoringService.getLogEntries();

        // Apply filters
        if (logLevel != null && !"All".equals(logLevel)) {
          logs = logs.stream()
              .filter(log -> logLevel.equalsIgnoreCase(log.level()))
              .collect(Collectors.toList());
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
          logs = logs.stream()
              .filter(log -> log.message().toLowerCase().contains(searchTerm.toLowerCase()) ||
                  log.logger().toLowerCase().contains(searchTerm.toLowerCase()))
              .collect(Collectors.toList());
        }

        logsGrid.setItems(logs);
      } catch (Exception e) {
        showNotification("Error loading logs: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    }

    private void forceHealthCheck (String backendId){
      try {
        monitoringService.forceHealthCheck(backendId);
        updateHealthStatus();
        showNotification("Health check triggered for backend", NotificationVariant.LUMO_SUCCESS);
      } catch (Exception e) {
        showNotification("Error triggering health check: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    }

    private void clearLogs () {
      try {
        monitoringService.clearLogs();
        updateLogs();
        showNotification("Logs cleared successfully", NotificationVariant.LUMO_SUCCESS);
      } catch (Exception e) {
        showNotification("Error clearing logs: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    }

    private void showPerformance() {
      contentArea.removeAll();
      contentArea.add(createPerformanceMonitoring());
    }
  
    private Component createMonitoringOverview() {
      VerticalLayout overviewLayout = new VerticalLayout();
      overviewLayout.setSizeFull();
      overviewLayout.setPadding(true);
      overviewLayout.setSpacing(true);
  
      H3 title = new H3("Monitoring Overview");
      title.addClassName("kong-fade-in");
  
      HorizontalLayout topRow = new HorizontalLayout();
      topRow.setSizeFull();
      topRow.setSpacing(true);
  
      topRow.add(createSystemHealthCard(), createActiveAlertsCard(), createThroughputCard());
  
      HorizontalLayout bottomRow = new HorizontalLayout();
      bottomRow.setSizeFull();
      bottomRow.setSpacing(true);
  
      bottomRow.add(createRecentActivityCard(), createTopEndpointsCard());
  
      overviewLayout.add(title, topRow, bottomRow);
      return overviewLayout;
    }
  
    private Component createPerformanceMonitoring() {
      VerticalLayout performanceLayout = new VerticalLayout();
      performanceLayout.setSizeFull();
      performanceLayout.setPadding(true);
      performanceLayout.setSpacing(true);
  
      H3 title = new H3("Performance Monitoring");
      title.addClassName("kong-fade-in");
  
      HorizontalLayout chartsRow1 = new HorizontalLayout();
      chartsRow1.setSizeFull();
      chartsRow1.setSpacing(true);
  
      chartsRow1.add(createResponseTimeChart(), createThroughputTrendChart());
  
      HorizontalLayout chartsRow2 = new HorizontalLayout();
      chartsRow2.setSizeFull();
      chartsRow2.setSpacing(true);
  
      chartsRow2.add(createErrorRateChart(), createResourceUsageChart());
  
      performanceLayout.add(title, chartsRow1, chartsRow2);
      return performanceLayout;
    }
  
    private Component createSystemHealthCard() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4("System Health");
      Span healthValue = new Span("98.5%");
      healthValue.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
      healthValue.getStyle().set("color", "var(--kong-success)");
  
      Span uptimeValue = new Span("Uptime: 15d 8h 32m");
      uptimeValue.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
  
      card.add(title, healthValue, uptimeValue);
      return card;
    }
  
    private Component createActiveAlertsCard() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4("Active Alerts");
      Span alertValue = new Span("3");
      alertValue.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
      alertValue.getStyle().set("color", "var(--kong-warning)");
  
      Span criticalValue = new Span("2 Critical, 1 Warning");
      criticalValue.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
  
      card.add(title, alertValue, criticalValue);
      return card;
    }
  
    private Component createThroughputCard() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4("Current Throughput");
      Span throughputValue = new Span("1,247 req/s");
      throughputValue.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);
      throughputValue.getStyle().set("color", "var(--kong-primary)");
  
      Span peakValue = new Span("Peak: 1,892 req/s");
      peakValue.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
  
      card.add(title, throughputValue, peakValue);
      return card;
    }
  
    private Component createRecentActivityCard() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4("Recent Activity");
  
      VerticalLayout activityList = new VerticalLayout();
      activityList.setPadding(false);
      activityList.setSpacing(false);
  
      activityList.add(createActivityItem("Backend 'api-server' health check failed", "2 min ago", "error"));
      activityList.add(createActivityItem("High memory usage detected", "5 min ago", "warning"));
      activityList.add(createActivityItem("New consumer registered", "8 min ago", "info"));
      activityList.add(createActivityItem("Route configuration updated", "12 min ago", "success"));
  
      card.add(title, activityList);
      return card;
    }
  
    private Component createTopEndpointsCard() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4("Top Endpoints");
  
      VerticalLayout endpointList = new VerticalLayout();
      endpointList.setPadding(false);
      endpointList.setSpacing(false);
  
      endpointList.add(createEndpointItem("/api/users", "245 req/s"));
      endpointList.add(createEndpointItem("/api/orders", "189 req/s"));
      endpointList.add(createEndpointItem("/api/products", "156 req/s"));
      endpointList.add(createEndpointItem("/api/auth", "98 req/s"));
  
      card.add(title, endpointList);
      return card;
    }
  
    private Component createActivityItem(String message, String time, String type) {
      HorizontalLayout item = new HorizontalLayout();
      item.setWidthFull();
      item.setAlignItems(FlexComponent.Alignment.CENTER);
  
      Span messageSpan = new Span(message);
      messageSpan.addClassNames(LumoUtility.FontSize.SMALL);
  
      Span timeSpan = new Span(time);
      timeSpan.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
  
      Span indicator = new Span();
      indicator.addClassName("activity-indicator");
      switch (type) {
        case "error" -> indicator.getStyle().set("background-color", "var(--kong-error)");
        case "warning" -> indicator.getStyle().set("background-color", "var(--kong-warning)");
        case "info" -> indicator.getStyle().set("background-color", "var(--kong-info)");
        case "success" -> indicator.getStyle().set("background-color", "var(--kong-success)");
      }
  
      item.add(indicator, messageSpan);
      item.setFlexGrow(1, messageSpan);
      item.add(timeSpan);
  
      return item;
    }
  
    private Component createEndpointItem(String endpoint, String rate) {
      HorizontalLayout item = new HorizontalLayout();
      item.setWidthFull();
      item.setAlignItems(FlexComponent.Alignment.CENTER);
  
      Span endpointSpan = new Span(endpoint);
      endpointSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);
  
      Span rateSpan = new Span(rate);
      rateSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
  
      item.add(endpointSpan);
      item.setFlexGrow(1, endpointSpan);
      item.add(rateSpan);
  
      return item;
    }
  
    private Component createResponseTimeChart() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4(translationService.getTranslation("monitoring.response.time.chart"));
      Div chartContainer = createSimpleLineChart();
      chartContainer.setHeight("250px");
  
      card.add(title, chartContainer);
      return card;
    }
  
    private Component createThroughputTrendChart() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4(translationService.getTranslation("monitoring.throughput.chart"));
      Div chartContainer = createSimpleAreaChart();
      chartContainer.setHeight("250px");
  
      card.add(title, chartContainer);
      return card;
    }
  
    private Component createErrorRateChart() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4(translationService.getTranslation("monitoring.error.rate.chart"));
      Div chartContainer = createSimpleLineChart();
      chartContainer.setHeight("250px");
  
      card.add(title, chartContainer);
      return card;
    }
  
    private Component createResourceUsageChart() {
      VerticalLayout card = new VerticalLayout();
      card.addClassName("kong-metrics-card");
      card.setFlexGrow(1);
  
      H4 title = new H4(translationService.getTranslation("monitoring.resource.usage.chart"));
      Div chartContainer = createSimpleMultiLineChart();
      chartContainer.setHeight("250px");
  
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
        int height = 20 + (int)(Math.random() * 60); // Random height between 20-80
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
      String points = "0,80 8,75 17,70 25,65 33,60 42,55 50,60 58,50 67,55 75,45 83,50 92,40 100,45";
      area.getStyle().set("clip-path", "polygon(" + points + ")");
      chart.add(area);
  
      return chart;
    }
  
    private Div createSimpleMultiLineChart() {
      Div chart = new Div();
      chart.addClassName("kong-simple-chart");
      chart.addClassName("kong-multi-line-chart");
  
      // Create CPU line
      Div cpuLine = new Div();
      cpuLine.addClassName("kong-chart-line");
      cpuLine.addClassName("cpu-line");
      String cpuPoints = "0,70 10,65 20,60 30,55 40,60 50,50 60,55 70,45 80,50 90,40 100,45";
      cpuLine.getStyle().set("clip-path", "polygon(" + cpuPoints + ")");
      chart.add(cpuLine);
  
      // Create Memory line
      Div memoryLine = new Div();
      memoryLine.addClassName("kong-chart-line");
      memoryLine.addClassName("memory-line");
      String memoryPoints = "0,80 10,75 20,70 30,65 40,70 50,60 60,65 70,55 80,60 90,50 100,55";
      memoryLine.getStyle().set("clip-path", "polygon(" + memoryPoints + ")");
      chart.add(memoryLine);
  
      return chart;
    }
  
    private void showNotification (String message, NotificationVariant variant){
      Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
      notification.addThemeVariants(variant);
    }
  }