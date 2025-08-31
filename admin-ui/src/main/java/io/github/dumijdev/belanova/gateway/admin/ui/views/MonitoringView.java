package io.github.dumijdev.belanova.gateway.admin.views.monitoring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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

  public MonitoringView(MonitoringService monitoringService) {
    this.monitoringService = monitoringService;
    this.tabSheet = new Tabs();
    this.contentArea = new VerticalLayout();
    this.healthGrid = new Grid<>(BackendHealthStatus.class, false);
    this.metricsGrid = new Grid<>(MetricInfo.class, false);
    this.logsGrid = new Grid<>(LogEntry.class, false);
    this.logSearchField = new TextField();
    this.logLevelFilter = new Select<>();

    setSizeFull();
    setPadding(false);

    configureTabs();
    configureGrids();

    add(tabSheet, contentArea);

    // Default to health check tab
    showHealthCheck();
  }

  private void configureTabs() {
    Tab healthTab = new Tab(new Icon(VaadinIcon.HEART), new Span("Health Checks"));
    Tab metricsTab = new Tab(new Icon(VaadinIcon.BAR_CHART), new Span("Metrics"));
    Tab logsTab = new Tab(new Icon(VaadinIcon.FILE_TEXT), new Span("Logs"));

    tabSheet.add(healthTab, metricsTab, logsTab);

    tabSheet.addSelectedChangeListener(event -> {
      Tab selectedTab = event.getSelectedTab();
      if (selectedTab == healthTab) {
        showHealthCheck();
      } else if (selectedTab == metricsTab) {
        showMetrics();
      } else if (selectedTab == logsTab) {
        showLogs();
      }
    });
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

    metricsGrid.addColumn(MetricInfo::getUnit)
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
    Span badge = new Span(health.getStatus());
    badge.getElement().getThemeList().add("badge");

    switch (health.getStatus().toLowerCase()) {
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
    checkButton.addClickListener(e -> forceHealthCheck(health.getBackendId()));

    return checkButton;
  }

  private Component createHealthDetails(BackendHealthStatus health) {
    VerticalLayout details = new VerticalLayout();
    details.setPadding(true);
    details.addClassNames(LumoUtility.Background.CONTRAST_5);

    private Component createHealthDetails (BackendHealthStatus health){
      VerticalLayout details = new VerticalLayout();
      details.setPadding(true);
      details.addClassNames(LumoUtility.Background.CONTRAST_5);

      if (health.getErrorMessage() != null && !health.getErrorMessage().isEmpty()) {
        H4 errorTitle = new H4("Error Details");
        Pre errorPre = new Pre(health.getErrorMessage());
        errorPre.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.ERROR);
        details.add(errorTitle, errorPre);
      }

      // Health check configuration
      H4 configTitle = new H4("Health Check Configuration");
      Span healthPath = new Span("Health Path: " + (health.getHealthPath() != null ? health.getHealthPath() : "N/A"));
      Span interval = new Span("Check Interval: " + health.getCheckInterval() + "s");
      Span timeout = new Span("Timeout: " + health.getTimeout() + "s");

      VerticalLayout configLayout = new VerticalLayout(healthPath, interval, timeout);
      configLayout.setPadding(false);
      configLayout.setSpacing(false);

      details.add(configTitle, configLayout);
      return details;
    }

    private Component createLogLevelBadge (LogEntry log){
      Span badge = new Span(log.getLevel());
      badge.getElement().getThemeList().add("badge");

      switch (log.getLevel().toUpperCase()) {
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

      if (log.getStackTrace() != null && !log.getStackTrace().isEmpty()) {
        H4 stackTitle = new H4("Stack Trace");
        Pre stackPre = new Pre(log.getStackTrace());
        stackPre.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontFamily.MONOSPACE);
        stackPre.getStyle().set("max-height", "300px");
        stackPre.getStyle().set("overflow", "auto");
        details.add(stackTitle, stackPre);
      }

      if (log.getRequestId() != null) {
        Span requestIdSpan = new Span("Request ID: " + log.getRequestId());
        requestIdSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontFamily.MONOSPACE);
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
              .filter(log -> logLevel.equalsIgnoreCase(log.getLevel()))
              .collect(Collectors.toList());
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
          logs = logs.stream()
              .filter(log -> log.getMessage().toLowerCase().contains(searchTerm.toLowerCase()) ||
                  log.getLogger().toLowerCase().contains(searchTerm.toLowerCase()))
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

    private void showNotification (String message, NotificationVariant variant){
      Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
      notification.addThemeVariants(variant);
    }
  }