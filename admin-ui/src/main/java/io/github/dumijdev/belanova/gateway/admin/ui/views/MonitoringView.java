package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RouteMetrics;
import io.github.dumijdev.belanova.gateway.admin.ui.services.MonitoringService;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;

import java.time.format.DateTimeFormatter;

@Route(value = "monitoring", layout = MainLayout.class)
@PageTitle("Monitoring")
@CssImport("./styles/monitoring.css")
public class MonitoringView extends VerticalLayout {

  private final MonitoringService monitoringService;
  private final TranslationService translationService;
  private final Grid<RouteMetrics> metricsGrid = new Grid<>(RouteMetrics.class, false);

  public MonitoringView(MonitoringService monitoringService, TranslationService translationService) {
    this.monitoringService = monitoringService;
    this.translationService = translationService;

    setSizeFull();
    setPadding(false);
    setSpacing(false);
    addClassName("monitoring-view");

    add(createHeader());
    add(createOverviewSection());
    add(createMetricsSection());

    loadData();
  }

  private Component createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("view-header");
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);

    // Title section
    VerticalLayout titleSection = new VerticalLayout();
    titleSection.setSpacing(false);
    titleSection.setPadding(false);

    H1 title = new H1(translationService.getTranslation("navigation.monitoring"));
    title.addClassName("view-title");

    Span subtitle = new Span("Real-time performance metrics and system health monitoring");
    subtitle.addClassName("view-subtitle");

    titleSection.add(title, subtitle);

    // Control section
    HorizontalLayout controls = new HorizontalLayout();
    controls.setSpacing(true);

    Select<String> timeRange = new Select<>();
    timeRange.setItems("Last Hour", "Last 24 Hours", "Last 7 Days", "Last 30 Days");
    timeRange.setValue("Last Hour");
    timeRange.addClassName("time-range-select");

    Button autoRefreshBtn = new Button("Auto Refresh");
    autoRefreshBtn.setIcon(VaadinIcon.REFRESH.create());
    autoRefreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    autoRefreshBtn.addClassName("auto-refresh-btn");
    autoRefreshBtn.addClickListener(e -> {
      loadData();
      Notification.show("Data refreshed");
    });

    Button exportBtn = new Button("Export");
    exportBtn.setIcon(VaadinIcon.DOWNLOAD.create());
    exportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    exportBtn.addClassName("export-btn");

    controls.add(timeRange, autoRefreshBtn, exportBtn);

    header.add(titleSection, controls);
    return header;
  }

  private Component createOverviewSection() {
    FormLayout grid = new FormLayout();
    grid.addClassName("overview-grid");
    grid.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep("600px", 2),
        new FormLayout.ResponsiveStep("1024px", 4)
    );

    grid.add(
        createHealthScoreCard(),
        createTotalRoutesCard(),
        createAvgResponseTimeCard(),
        createErrorRateCard(),
        createResponseTimeChart(),
        createThroughputChart()
    );

    return grid;
  }

  private Component createHealthScoreCard() {
    Div card = new Div();
    card.addClassName("metric-card health-score-card");

    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H3 title = new H3("Overall Health Score");
    title.addClassName("card-title");

    Icon icon = VaadinIcon.HEART.create();
    icon.addClassNames("card-icon", "health-icon");

    header.add(title, icon);

    VerticalLayout content = new VerticalLayout();
    content.addClassName("card-content");
    content.setSpacing(false);

    Span score = new Span("92%");
    score.addClassNames("main-metric" ,"health-score");

    ProgressBar healthBar = new ProgressBar(0, 100, 92);
    healthBar.addClassName("health-progress-bar");

    Span details = new Span("Excellent system performance");
    details.addClassNames("metric-details", "success");

    content.add(score, healthBar, details);
    card.add(header, content);

    return card;
  }

  private Component createTotalRoutesCard() {
    return createSimpleMetricCard(
        "Active Routes",
        "147",
        "+12 since yesterday",
        VaadinIcon.ROAD,
        "routes-icon"
    );
  }

  private Component createAvgResponseTimeCard() {
    return createSimpleMetricCard(
        "Avg Response Time",
        "142ms",
        "-23ms from yesterday",
        VaadinIcon.TIMER,
        "timer-icon"
    );
  }

  private Component createErrorRateCard() {
    return createSimpleMetricCard(
        "Error Rate",
        "0.3%",
        "Within normal range",
        VaadinIcon.EXCLAMATION_CIRCLE_O,
        "error-icon"
    );
  }

  private Component createSimpleMetricCard(String title, String value, String change,
                                           VaadinIcon iconType, String iconClass) {
    Div card = new Div();
    card.addClassName("metric-card");

    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H3 cardTitle = new H3(title);
    cardTitle.addClassName("card-title");

    Icon icon = iconType.create();
    icon.addClassName("card-icon " + iconClass);

    header.add(cardTitle, icon);

    VerticalLayout content = new VerticalLayout();
    content.addClassName("card-content");
    content.setSpacing(false);

    Span mainValue = new Span(value);
    mainValue.addClassName("main-metric");

    Span changeText = new Span(change);
    changeText.addClassName("metric-change");

    content.add(mainValue, changeText);
    card.add(header, content);

    return card;
  }

  private Component createResponseTimeChart() {
    Div card = new Div();
    card.addClassNames("chart-card", "response-time-chart");

    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H2 title = new H2("Response Time Trends");
    title.addClassName("card-title");

    HorizontalLayout chartControls = new HorizontalLayout();
    Button realTimeBtn = new Button("Real-time");
    realTimeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

    Button averageBtn = new Button("Average");
    averageBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

    Button p95Btn = new Button("95th %");
    p95Btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

    chartControls.add(realTimeBtn, averageBtn, p95Btn);
    chartControls.addClassName("chart-controls");

    header.add(title, chartControls);

    Div chartArea = new Div();
    chartArea.addClassName("chart-area");

    // Simulated chart data visualization
    VerticalLayout chartContent = new VerticalLayout();
    chartContent.addClassName("chart-simulation");
    chartContent.setSpacing(false);

    for (int i = 0; i < 8; i++) {
      HorizontalLayout dataPoint = new HorizontalLayout();
      dataPoint.addClassName("chart-data-point");

      Span time = new Span(String.format("%02d:00", 14 + i));
      time.addClassName("chart-time");

      ProgressBar responseBar = new ProgressBar(0, 500, 100 + (i * 20) + (int)(Math.random() * 50));
      responseBar.addClassName("response-bar");

      Span value = new Span((100 + (i * 20) + (int)(Math.random() * 50)) + "ms");
      value.addClassName("chart-value");

      dataPoint.add(time, responseBar, value);
      chartContent.add(dataPoint);
    }

    chartArea.add(chartContent);
    card.add(header, chartArea);

    return card;
  }

  private Component createThroughputChart() {
    Div card = new Div();
    card.addClassNames("chart-card", "throughput-chart");

    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");

    H2 title = new H2("Request Throughput");
    title.addClassName("card-title");

    header.add(title);

    Div chartArea = new Div();
    chartArea.addClassNames("chart-area", "throughput-area");

    // Simulated throughput metrics
    VerticalLayout throughputMetrics = new VerticalLayout();
    throughputMetrics.addClassName("throughput-metrics");
    throughputMetrics.setSpacing(false);

    HorizontalLayout currentRps = new HorizontalLayout();
    currentRps.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    currentRps.setWidthFull();
    currentRps.add(new Span("Current RPS:"), new Span("1,247"));
    currentRps.addClassNames("metric-row", "current");

    HorizontalLayout peakRps = new HorizontalLayout();
    peakRps.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    peakRps.setWidthFull();
    peakRps.add(new Span("Peak RPS:"), new Span("2,891"));
    peakRps.addClassNames("metric-row", "peak");

    HorizontalLayout avgRps = new HorizontalLayout();
    avgRps.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    avgRps.setWidthFull();
    avgRps.add(new Span("Average RPS:"), new Span("1,456"));
    avgRps.addClassNames("metric-row", "average");

    // Mini sparkline representation
    Div sparkline = new Div();
    sparkline.addClassName("sparkline-container");
    sparkline.add(new Span("📊 Real-time throughput visualization"));

    throughputMetrics.add(currentRps, peakRps, avgRps, sparkline);
    chartArea.add(throughputMetrics);
    card.add(header, chartArea);

    return card;
  }

  private Component createMetricsSection() {
    VerticalLayout section = new VerticalLayout();
    section.addClassName("metrics-section");
    section.setPadding(false);

    H2 sectionTitle = new H2("Route Performance Metrics");
    sectionTitle.addClassName("section-title");

    HorizontalLayout tableHeader = new HorizontalLayout();
    tableHeader.addClassName("table-header");
    tableHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    tableHeader.setAlignItems(FlexComponent.Alignment.CENTER);

    Button refreshTableBtn = new Button("Refresh Data");
    refreshTableBtn.setIcon(VaadinIcon.REFRESH.create());
    refreshTableBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshTableBtn.addClickListener(e -> loadData());

    tableHeader.add(sectionTitle, refreshTableBtn);

    Component grid = createMetricsGrid();

    section.add(tableHeader, grid);
    return section;
  }

  private Component createMetricsGrid() {
    metricsGrid.addClassName("metrics-grid");
    metricsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

    // Status column with visual indicator
    metricsGrid.addComponentColumn(this::createStatusIndicator)
        .setHeader("Status")
        .setWidth("100px")
        .setFlexGrow(0);

    // Route name
    metricsGrid.addColumn(RouteMetrics::routeName)
        .setHeader("Route")
        .setSortable(true)
        .setWidth("200px");

    // Response time with color coding
    metricsGrid.addComponentColumn(this::createResponseTimeIndicator)
        .setHeader("Response Time")
        .setWidth("150px")
        .setFlexGrow(0);

    // Success rate with progress bar
    metricsGrid.addComponentColumn(this::createSuccessRateIndicator)
        .setHeader("Success Rate")
        .setWidth("150px")
        .setFlexGrow(0);

    // Last check time
    metricsGrid.addColumn(metrics -> metrics.lastCheck() != null ?
            metrics.lastCheck().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "Never")
        .setHeader("Last Check")
        .setWidth("120px")
        .setFlexGrow(0);

    // Uptime percentage
    metricsGrid.addComponentColumn(this::createUptimeIndicator)
        .setHeader("Uptime")
        .setWidth("120px")
        .setFlexGrow(0);

    // Actions
    metricsGrid.addComponentColumn(this::createActionsColumn)
        .setHeader("Actions")
        .setWidth("150px")
        .setFlexGrow(0);

    return metricsGrid;
  }

  private Component createStatusIndicator(RouteMetrics metrics) {
    HorizontalLayout status = new HorizontalLayout();
    status.setAlignItems(FlexComponent.Alignment.CENTER);
    status.setSpacing(false);
    status.addClassName("status-indicator");

    Icon statusIcon;
    String statusClass;

    switch (metrics.status().toLowerCase()) {
      case "healthy" -> {
        statusIcon = VaadinIcon.CHECK_CIRCLE.create();
        statusClass = "status-healthy";
      }
      case "degraded" -> {
        statusIcon = VaadinIcon.WARNING.create();
        statusClass = "status-warning";
      }
      case "down" -> {
        statusIcon = VaadinIcon.CLOSE_CIRCLE.create();
        statusClass = "status-error";
      }
      default -> {
        statusIcon = VaadinIcon.QUESTION_CIRCLE.create();
        statusClass = "status-unknown";
      }
    }

    statusIcon.addClassNames("status-icon ", statusClass);

    Span statusText = new Span(metrics.status());
    statusText.addClassName("status-text");

    status.add(statusIcon, statusText);
    return status;
  }

  private Component createResponseTimeIndicator(RouteMetrics metrics) {
    HorizontalLayout indicator = new HorizontalLayout();
    indicator.setAlignItems(FlexComponent.Alignment.CENTER);
    indicator.addClassName("response-time-indicator");

    Span timeValue = new Span(metrics.responseTime() + "ms");
    timeValue.addClassName("response-time-value");

    // Color code based on response time
    String timeClass = switch (metrics.responseTime()) {
      case Integer i when i < 100 -> "time-excellent";
      case Integer i when i < 300 -> "time-good";
      case Integer i when i < 500 -> "time-warning";
      default -> "time-poor";
    };

    timeValue.addClassName(timeClass);
    indicator.add(timeValue);

    return indicator;
  }

  private Component createSuccessRateIndicator(RouteMetrics metrics) {
    VerticalLayout container = new VerticalLayout();
    container.setSpacing(false);
    container.setPadding(false);
    container.addClassName("success-rate-container");

    HorizontalLayout header = new HorizontalLayout();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setWidthFull();

    Span rateText = new Span(String.format("%.1f%%", metrics.successRate()));
    rateText.addClassName("success-rate-text");

    header.add(rateText);

    ProgressBar rateBar = new ProgressBar(0, 100, metrics.successRate());
    rateBar.addClassName("success-rate-bar");

    // Color coding for success rate
    if (metrics.successRate() >= 99.0) {
      rateBar.addClassName("rate-excellent");
    } else if (metrics.successRate() >= 95.0) {
      rateBar.addClassName("rate-good");
    } else if (metrics.successRate() >= 90.0) {
      rateBar.addClassName("rate-warning");
    } else {
      rateBar.addClassName("rate-poor");
    }

    container.add(header, rateBar);
    return container;
  }

  private Component createUptimeIndicator(RouteMetrics metrics) {
    Span uptime = new Span(String.format("%.2f%%", metrics.uptime()));
    uptime.addClassName("uptime-indicator");

    // Color coding for uptime
    if (metrics.uptime() >= 99.9) {
      uptime.addClassName("uptime-excellent");
    } else if (metrics.uptime() >= 99.0) {
      uptime.addClassName("uptime-good");
    } else if (metrics.uptime() >= 95.0) {
      uptime.addClassName("uptime-warning");
    } else {
      uptime.addClassName("uptime-poor");
    }

    return uptime;
  }

  private Component createActionsColumn(RouteMetrics metrics) {
    HorizontalLayout actions = new HorizontalLayout();
    actions.setSpacing(false);
    actions.addClassName("actions-column");

    Button checkBtn = new Button(VaadinIcon.PLAY.create());
    checkBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
    checkBtn.addClassNames("action-btn", "check-btn");
    checkBtn.setTooltipText("Run health check");
    checkBtn.addClickListener(e -> {
      monitoringService.check(metrics.routeId());
      Notification.show("Health check initiated for " + metrics.routeName());
      loadData();
    });

    Button detailsBtn = new Button(VaadinIcon.EYE.create());
    detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
    detailsBtn.addClassNames("action-btn", "details-btn");
    detailsBtn.setTooltipText("View details");

    actions.add(checkBtn, detailsBtn);
    return actions;
  }

  private void loadData() {
    metricsGrid.setItems(monitoringService.fetchMetrics());
  }
}