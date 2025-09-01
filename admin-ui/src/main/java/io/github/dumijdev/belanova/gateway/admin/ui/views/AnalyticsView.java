package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.AnalyticsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@PageTitle("Analytics")
@Route(value = "analytics", layout = MainLayout.class)
public class AnalyticsView extends VerticalLayout {

  private final AnalyticsService analyticsService;
  private ComboBox<String> timeRangeSelector;
  private ComboBox<String> serviceSelector;

  public AnalyticsView(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    // Kong-inspired styling
    getStyle().set("background", "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)");

    add(createHeader(), createFilters(), createAnalyticsGrid());
  }

  private Component createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM);

    H2 title = new H2("API Analytics");
    title.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontWeight.BOLD);

    Span lastUpdated = new Span("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    lastUpdated.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.SMALL);

    header.add(title, lastUpdated);
    return header;
  }

  private Component createFilters() {
    HorizontalLayout filters = new HorizontalLayout();
    filters.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL);

    timeRangeSelector = new ComboBox<>("Time Range");
    timeRangeSelector.setItems("Last 15 minutes", "Last hour", "Last 24 hours", "Last 7 days", "Last 30 days");
    timeRangeSelector.setValue("Last 24 hours");
    timeRangeSelector.addValueChangeListener(e -> updateAnalytics());

    serviceSelector = new ComboBox<>("Service");
    serviceSelector.setItems("All Services", "User Service", "Order Service", "Payment Service", "Notification Service");
    serviceSelector.setValue("All Services");
    serviceSelector.addValueChangeListener(e -> updateAnalytics());

    Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
    refreshButton.getElement().setAttribute("kong-primary", "");
    refreshButton.addClickListener(e -> updateAnalytics());

    filters.add(timeRangeSelector, serviceSelector, refreshButton);
    return filters;
  }

  private Component createAnalyticsGrid() {
    VerticalLayout analyticsContainer = new VerticalLayout();
    analyticsContainer.setSizeFull();
    analyticsContainer.addClassNames(LumoUtility.Padding.LARGE);
    analyticsContainer.setSpacing(true);

    // Key Metrics Row
    Component keyMetrics = createKeyMetricsRow();
    analyticsContainer.add(keyMetrics);

    // Performance Metrics Row
    Component performanceMetrics = createPerformanceMetricsRow();
    analyticsContainer.add(performanceMetrics);

    // Traffic Analysis
    Component trafficAnalysis = createTrafficAnalysis();
    analyticsContainer.add(trafficAnalysis);

    // Error Analysis
    Component errorAnalysis = createErrorAnalysis();
    analyticsContainer.add(errorAnalysis);

    return analyticsContainer;
  }

  private Component createKeyMetricsRow() {
    HorizontalLayout metricsRow = new HorizontalLayout();
    metricsRow.setWidthFull();
    metricsRow.setSpacing(true);

    // Total Requests
    VerticalLayout totalRequests = createMetricCard(
        "Total Requests",
        "1,247,891",
        "+12.5%",
        VaadinIcon.TRENDING_UP,
        "success"
    );

    // Success Rate
    VerticalLayout successRate = createMetricCard(
        "Success Rate",
        "98.7%",
        "+0.3%",
        VaadinIcon.CHECK_CIRCLE,
        "success"
    );

    // Average Response Time
    VerticalLayout avgResponseTime = createMetricCard(
        "Avg Response Time",
        "145ms",
        "-8.2%",
        VaadinIcon.TIMER,
        "warning"
    );

    // Error Rate
    VerticalLayout errorRate = createMetricCard(
        "Error Rate",
        "1.3%",
        "-0.1%",
        VaadinIcon.WARNING,
        "error"
    );

    metricsRow.add(totalRequests, successRate, avgResponseTime, errorRate);
    return metricsRow;
  }

  private Component createPerformanceMetricsRow() {
    HorizontalLayout metricsRow = new HorizontalLayout();
    metricsRow.setWidthFull();
    metricsRow.setSpacing(true);

    // P95 Response Time
    VerticalLayout p95ResponseTime = createMetricCard(
        "P95 Response Time",
        "234ms",
        "+2.1%",
        VaadinIcon.CHART_LINE,
        "warning"
    );

    // Throughput
    VerticalLayout throughput = createMetricCard(
        "Throughput",
        "1,247 req/s",
        "+15.3%",
        VaadinIcon.BOLT,
        "success"
    );

    // Active Connections
    VerticalLayout activeConnections = createMetricCard(
        "Active Connections",
        "892",
        "+5.7%",
        VaadinIcon.CONNECT,
        "primary"
    );

    // Bandwidth Usage
    VerticalLayout bandwidthUsage = createMetricCard(
        "Bandwidth",
        "2.4 GB",
        "+8.9%",
        VaadinIcon.DOWNLOAD,
        "contrast"
    );

    metricsRow.add(p95ResponseTime, throughput, activeConnections, bandwidthUsage);
    return metricsRow;
  }

  private VerticalLayout createMetricCard(String title, String value, String change, VaadinIcon icon, String theme) {
    VerticalLayout card = new VerticalLayout();
    card.addClassNames(
        "kong-metrics-card",
        LumoUtility.BorderRadius.LARGE,
        LumoUtility.BoxShadow.MEDIUM,
        LumoUtility.Padding.LARGE
    );
    card.setFlexGrow(1);

    // Kong-inspired styling
    card.getStyle()
        .set("background", "linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)")
        .set("backdrop-filter", "blur(10px)")
        .set("border", "1px solid rgba(255,255,255,0.2)");

    Icon metricIcon = icon.create();
    metricIcon.addClassNames("kong-card-icon");

    H3 metricTitle = new H3(title);
    metricTitle.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.NONE);

    Span metricValue = new Span(value);
    metricValue.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD);

    Span metricChange = new Span(change);
    if (change.startsWith("+")) {
      metricChange.addClassNames(LumoUtility.TextColor.SUCCESS);
    } else if (change.startsWith("-")) {
      metricChange.addClassNames(LumoUtility.TextColor.SUCCESS);
    } else {
      metricChange.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST);
    }
    metricChange.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);

    HorizontalLayout headerLayout = new HorizontalLayout(metricIcon);
    headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

    card.add(headerLayout, metricTitle, metricValue, metricChange);
    card.setAlignItems(FlexComponent.Alignment.START);

    return card;
  }

  private Component createTrafficAnalysis() {
    VerticalLayout trafficCard = new VerticalLayout();
    trafficCard.addClassNames(
        "kong-metrics-card",
        LumoUtility.BorderRadius.LARGE,
        LumoUtility.BoxShadow.MEDIUM,
        LumoUtility.Padding.LARGE
    );

    trafficCard.getStyle()
        .set("background", "linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)")
        .set("backdrop-filter", "blur(10px)")
        .set("border", "1px solid rgba(255,255,255,0.2)");

    H3 title = new H3("Traffic Analysis");
    title.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Margin.Bottom.MEDIUM);

    // Top Endpoints
    VerticalLayout endpointsLayout = new VerticalLayout();
    endpointsLayout.setPadding(false);
    endpointsLayout.setSpacing(false);

    String[] endpoints = {"/api/users", "/api/orders", "/api/payments", "/api/notifications"};
    int[] requestCounts = {45231, 38765, 28934, 19876};

    for (int i = 0; i < endpoints.length; i++) {
      HorizontalLayout endpointRow = new HorizontalLayout();
      endpointRow.setWidthFull();
      endpointRow.setAlignItems(FlexComponent.Alignment.CENTER);

      Span endpointName = new Span(endpoints[i]);
      endpointName.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.SMALL);

      Span requestCount = new Span(requestCounts[i] + " req");
      requestCount.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);

      ProgressBar progressBar = new ProgressBar();
      progressBar.setValue(requestCounts[i] / 50000.0);
      progressBar.setWidth("100px");
      progressBar.getStyle().set("height", "6px");

      endpointRow.add(endpointName, progressBar, requestCount);
      endpointRow.setFlexGrow(1, endpointName);
      endpointsLayout.add(endpointRow);
    }

    trafficCard.add(title, endpointsLayout);
    return trafficCard;
  }

  private Component createErrorAnalysis() {
    VerticalLayout errorCard = new VerticalLayout();
    errorCard.addClassNames(
        "kong-metrics-card",
        LumoUtility.BorderRadius.LARGE,
        LumoUtility.BoxShadow.MEDIUM,
        LumoUtility.Padding.LARGE
    );

    errorCard.getStyle()
        .set("background", "linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)")
        .set("backdrop-filter", "blur(10px)")
        .set("border", "1px solid rgba(255,255,255,0.2)");

    H3 title = new H3("Error Analysis");
    title.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Margin.Bottom.MEDIUM);

    // Error breakdown
    VerticalLayout errorsLayout = new VerticalLayout();
    errorsLayout.setPadding(false);
    errorsLayout.setSpacing(false);

    String[] errorTypes = {"4xx Client Errors", "5xx Server Errors", "Timeout Errors", "Network Errors"};
    int[] errorCounts = {1247, 234, 89, 45};
    String[] errorColors = {"warning", "error", "contrast", "primary"};

    for (int i = 0; i < errorTypes.length; i++) {
      HorizontalLayout errorRow = new HorizontalLayout();
      errorRow.setWidthFull();
      errorRow.setAlignItems(FlexComponent.Alignment.CENTER);

      Span errorType = new Span(errorTypes[i]);
      errorType.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.SMALL);

      Span errorCount = new Span(errorCounts[i] + "");
      errorCount.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);

      Span errorBadge = new Span();
      errorBadge.getElement().getThemeList().add("badge " + errorColors[i]);
      errorBadge.getElement().setText(errorCounts[i] + "");

      errorRow.add(errorType, errorBadge);
      errorRow.setFlexGrow(1, errorType);
      errorsLayout.add(errorRow);
    }

    errorCard.add(title, errorsLayout);
    return errorCard;
  }

  private void updateAnalytics() {
    // In a real implementation, this would fetch new data based on filters
    // For now, we'll just refresh the display
    System.out.println("Updating analytics for: " + timeRangeSelector.getValue() + " - " + serviceSelector.getValue());
  }
}