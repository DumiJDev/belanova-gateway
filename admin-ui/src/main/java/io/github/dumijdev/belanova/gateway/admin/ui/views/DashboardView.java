package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
@CssImport("./styles/dashboard.css")
public class DashboardView extends VerticalLayout {

  private final TranslationService translationService;

  public DashboardView(TranslationService translationService) {
    this.translationService = translationService;

    setSizeFull();
    setPadding(false);
    setSpacing(false);
    addClassName("dashboard-view");

    add(createHeader());
    add(createMainContent());
  }

  private Component createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("dashboard-header");
    header.addClassName("view-header");
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);

    // Title section
    VerticalLayout titleSection = new VerticalLayout();
    titleSection.setSpacing(false);
    titleSection.setPadding(false);

    H1 title = new H1(translationService.getTranslation("navigation.dashboard"));
    title.addClassName("dashboard-title");
    title.addClassName("view-title");

    Span subtitle = new Span("Monitor your gateway's performance and health");
    subtitle.addClassName("dashboard-subtitle");
    subtitle.addClassName("view-subtitle");

    titleSection.add(title, subtitle);

    HorizontalLayout actions = new HorizontalLayout();
    actions.setSpacing(true);

    Button refreshBtn = new Button(translationService.getTranslation("common.refresh"), VaadinIcon.REFRESH.create());
    refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshBtn.addClassName("action-button");

    Button exportBtn = new Button(translationService.getTranslation("common.export"), VaadinIcon.DOWNLOAD.create());
    exportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    exportBtn.addClassName("action-button");

    actions.add(refreshBtn, exportBtn);
    header.add(titleSection, actions);
    return header;
  }

  private Component createMainContent() {
    FormLayout grid = new FormLayout();
    grid.addClassName("dashboard-grid");

    grid.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep("600px", 2),
        new FormLayout.ResponsiveStep("960px", 4)
    );

    // Row 1
    grid.add(
        createStatusCard(),
        createActiveRoutesCard(),
        createBackendHealthCard(),
        createRequestVolumeCard()
    );

    // Row 2
    grid.add(
        createTrafficChartCard(),
        createHealthTableCard()
    );

    // Row 3
    grid.add(
        createRecentAlertsCard(),
        createPluginStatusCard(),
        createSystemResourcesCard()
    );

    return grid;
  }

  private Component createStatusCard() {
    return createMetricCard(
        "Gateway Status",
        "OPERATIONAL",
        "Uptime: 12h 34m",
        VaadinIcon.CIRCLE_THIN,
        "status-healthy",
        "Last restart: 2025-08-29 08:15"
    );
  }

  private Component createActiveRoutesCard() {
    return createMetricCard(
        "Active Routes",
        "42",
        "↗ 5% since last hour",
        VaadinIcon.ROAD,
        "metric-positive",
        "All routes operational"
    );
  }

  private Component createBackendHealthCard() {
    Div card = new Div();
    card.addClassName("metric-card");

    // Header
    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H3 title = new H3("Backend Health");
    title.addClassName("card-title");

    Icon icon = VaadinIcon.HEART.create();
    icon.addClassName("card-icon");

    header.add(title, icon);

    // Content
    VerticalLayout content = new VerticalLayout();
    content.addClassName("card-content");
    content.setSpacing(false);

    Span mainMetric = new Span("8/10");
    mainMetric.addClassName("main-metric");

    Span description = new Span("healthy backends");
    description.addClassName("metric-description");

    // Progress indicator
    ProgressBar healthBar = new ProgressBar(0, 10, 8);
    healthBar.addClassName("health-progress");

    Span details = new Span("2 backends need attention");
    details.addClassNames("metric-details", "warning");

    content.add(mainMetric, description, healthBar, details);
    card.add(header, content);

    return card;
  }

  private Component createRequestVolumeCard() {
    Div card = new Div();
    card.addClassName("metric-card");

    // Header
    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H3 title = new H3("Requests/sec");
    title.addClassName("card-title");

    Icon icon = VaadinIcon.TRENDING_UP.create();
    icon.addClassName("card-icon");

    header.add(title, icon);

    // Content
    VerticalLayout content = new VerticalLayout();
    content.addClassName("card-content");
    content.setSpacing(false);

    Span mainMetric = new Span("1,247");
    mainMetric.addClassName("main-metric");

    // Mini sparkline placeholder
    Div sparkline = new Div();
    sparkline.addClassName("sparkline");
    sparkline.add(new Span("📈")); // Placeholder for actual chart

    HorizontalLayout stats = new HorizontalLayout();
    stats.addClassName("metric-stats");
    stats.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    stats.setWidthFull();

    Span peak = new Span("Peak: 1,500");
    peak.addClassName("stat-item");

    Span avg = new Span("Avg: 1,100");
    avg.addClassName("stat-item");

    stats.add(peak, avg);

    content.add(mainMetric, sparkline, stats);
    card.add(header, content);

    return card;
  }

  private Component createTrafficChartCard() {
    Div card = new Div();
    card.addClassNames("chart-card", "large-card");

    // Header
    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H2 title = new H2("Traffic Flow");
    title.addClassName("card-title");

    HorizontalLayout controls = new HorizontalLayout();
    Button hourlyBtn = new Button("1H");
    Button dailyBtn = new Button("24H");
    Button weeklyBtn = new Button("7D");

    hourlyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
    dailyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    weeklyBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

    controls.add(hourlyBtn, dailyBtn, weeklyBtn);
    controls.addClassName("chart-controls");

    header.add(title, controls);

    // Chart placeholder
    Div chartArea = new Div();
    chartArea.addClassName("chart-area");
    chartArea.add(new Span("📊 Real-time traffic chart will be rendered here"));

    card.add(header, chartArea);
    return card;
  }

  private Component createHealthTableCard() {
    Div card = new Div();
    card.addClassName("table-card");

    // Header
    H2 title = new H2("Backend Health Overview");
    title.addClassName("card-title");

    // Simple table representation
    VerticalLayout tableContent = new VerticalLayout();
    tableContent.addClassName("health-table");
    tableContent.setSpacing(false);

    tableContent.add(
        createHealthRow("User Service", "Healthy", "120ms", "status-healthy"),
        createHealthRow("Order Service", "Degraded", "250ms", "status-warning"),
        createHealthRow("Payment Service", "Healthy", "95ms", "status-healthy"),
        createHealthRow("Inventory Service", "Down", "N/A", "status-error")
    );

    card.add(title, tableContent);
    return card;
  }

  private Component createHealthRow(String name, String status, String responseTime, String statusClass) {
    HorizontalLayout row = new HorizontalLayout();
    row.addClassName("health-row");
    row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    row.setAlignItems(FlexComponent.Alignment.CENTER);
    row.setWidthFull();

    Span serviceName = new Span(name);
    serviceName.addClassName("service-name");

    Span statusBadge = new Span(status);
    statusBadge.addClassNames("status-badge", statusClass);

    Span response = new Span(responseTime);
    response.addClassName("response-time");

    row.add(serviceName, statusBadge, response);
    return row;
  }

  private Component createRecentAlertsCard() {
    Div card = new Div();
    card.addClassName("alert-card");

    H3 title = new H3("Recent Alerts");
    title.addClassName("card-title");

    VerticalLayout alerts = new VerticalLayout();
    alerts.addClassName("alerts-list");
    alerts.setSpacing(false);

    alerts.add(
        createAlertItem("High CPU usage detected", "2 min ago", "error"),
        createAlertItem("Backend X connection timeout", "5 min ago", "warning"),
        createAlertItem("Rate limit threshold reached", "12 min ago", "warning")
    );

    Button viewAllBtn = new Button("View all alerts →");
    viewAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    viewAllBtn.addClassName("view-all-btn");

    card.add(title, alerts, viewAllBtn);
    return card;
  }

  private Component createAlertItem(String message, String time, String severity) {
    HorizontalLayout item = new HorizontalLayout();
    item.addClassName("alert-item");
    item.setAlignItems(FlexComponent.Alignment.CENTER);

    Icon statusIcon = switch (severity) {
      case "error" -> VaadinIcon.EXCLAMATION_CIRCLE.create();
      case "warning" -> VaadinIcon.WARNING.create();
      default -> VaadinIcon.INFO_CIRCLE.create();
    };
    statusIcon.addClassNames("alert-icon", severity);

    VerticalLayout content = new VerticalLayout();
    content.setSpacing(false);
    content.setPadding(false);

    Span alertMessage = new Span(message);
    alertMessage.addClassName("alert-message");

    Span alertTime = new Span(time);
    alertTime.addClassName("alert-time");

    content.add(alertMessage, alertTime);
    item.add(statusIcon, content);

    return item;
  }

  private Component createPluginStatusCard() {
    return createSimpleCard(
        "Plugin Status",
        VaadinIcon.PLUG,
        new String[][]{
            {"Authentication", "Enabled"},
            {"Rate Limiting", "Enabled"},
            {"Circuit Breaker", "Disabled"},
            {"Logging", "Enabled"}
        }
    );
  }

  private Component createSystemResourcesCard() {
    Div card = new Div();
    card.addClassNames("metric-card", "resource-card");

    H3 title = new H3("System Resources");
    title.addClassName("card-title");

    VerticalLayout content = new VerticalLayout();
    content.addClassName("card-content");
    content.setSpacing(false);

    content.add(
        createResourceBar("CPU", 65, "cpu"),
        createResourceBar("Memory", 40, "memory"),
        createResourceBar("JVM Heap", 55, "heap")
    );

    card.add(title, content);
    return card;
  }

  private Component createResourceBar(String label, int percentage, String type) {
    VerticalLayout container = new VerticalLayout();
    container.setSpacing(false);
    container.setPadding(false);
    container.addClassName("resource-item");

    HorizontalLayout header = new HorizontalLayout();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setWidthFull();

    Span labelSpan = new Span(label);
    labelSpan.addClassName("resource-label");

    Span valueSpan = new Span(percentage + "% ");
    valueSpan.addClassName("resource-value");

    header.add(labelSpan, valueSpan);

    ProgressBar progressBar = new ProgressBar(0, 100, percentage);
    progressBar.addClassNames("resource-progress", type);

    container.add(header, progressBar);
    return container;
  }

  private Component createMetricCard(String title, String value, String change,
                                     VaadinIcon iconType, String statusClass, String details) {
    Div card = new Div();
    card.addClassName("metric-card");

    // Header
    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H3 cardTitle = new H3(title);
    cardTitle.addClassName("card-title");

    Icon icon = iconType.create();
    icon.addClassNames("card-icon ", statusClass);

    header.add(cardTitle, icon);

    // Content
    VerticalLayout content = new VerticalLayout();
    content.addClassName("card-content");
    content.setSpacing(false);

    Span mainValue = new Span(value);
    mainValue.addClassNames("main-metric", statusClass);

    Span changeIndicator = new Span(change);
    changeIndicator.addClassName("metric-change");

    Span detailsSpan = new Span(details);
    detailsSpan.addClassName("metric-details");

    content.add(mainValue, changeIndicator, detailsSpan);
    card.add(header, content);

    return card;
  }

  private Component createSimpleCard(String title, VaadinIcon iconType, String[][] items) {
    Div card = new Div();
    card.addClassName("simple-card");

    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("card-header");
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    H3 cardTitle = new H3(title);
    cardTitle.addClassName("card-title");

    Icon icon = iconType.create();
    icon.addClassName("card-icon");

    header.add(cardTitle, icon);

    VerticalLayout content = new VerticalLayout();
    content.addClassNames("card-content", "simple-content");
    content.setSpacing(false);

    for (String[] item : items) {
      HorizontalLayout row = new HorizontalLayout();
      row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
      row.setWidthFull();

      Span label = new Span(item[0]);
      label.addClassName("simple-label");

      Span value = new Span(item[1]);
      value.addClassNames("simple-value",
          (item[1].equals("Enabled") ? "status-healthy" : "status-disabled"));

      row.add(label, value);
      content.add(row);
    }

    card.add(header, content);
    return card;
  }
}
