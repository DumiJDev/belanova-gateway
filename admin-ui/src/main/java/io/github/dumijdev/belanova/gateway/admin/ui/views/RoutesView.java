package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
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
import io.github.dumijdev.belanova.gateway.admin.ui.models.RouteInfo;
import io.github.dumijdev.belanova.gateway.admin.ui.services.RouteService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Routes")
@Route(value = "routes", layout = MainLayout.class)
public class RoutesView extends VerticalLayout {

  private final RouteService routeService;
  private final TranslationService translationService;
  private final Grid<RouteInfo> grid;
  private final TextField searchField;
  private final Select<String> statusFilter;
  private final Tabs routesTabs;
  private final VerticalLayout contentArea;

  public RoutesView(RouteService routeService, TranslationService translationService) {
    this.routeService = routeService;
    this.translationService = translationService;
    this.grid = new Grid<>(RouteInfo.class, false);
    this.searchField = new TextField();
    this.statusFilter = new Select<>();
    this.routesTabs = new Tabs();
    this.contentArea = new VerticalLayout();

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    configureTabs();
    configureGrid();
    configureFilters();

    add(routesTabs, contentArea);

    // Default to routes list
    showRoutesList();
  }

  private void configureTabs() {
    Tab routesListTab = new Tab(new Icon(VaadinIcon.LIST), new Span(translationService.getTranslation("routes.list.tab")));
    Tab routesFlowTab = new Tab(new Icon(VaadinIcon.SPLINE_CHART), new Span(translationService.getTranslation("routes.flow.tab")));
    Tab routesAnalyticsTab = new Tab(new Icon(VaadinIcon.CHART), new Span(translationService.getTranslation("routes.analytics.tab")));

    routesTabs.add(routesListTab, routesFlowTab, routesAnalyticsTab);
    routesTabs.addClassName("kong-dashboard-tabs");

    routesTabs.addSelectedChangeListener(event -> {
      Tab selectedTab = event.getSelectedTab();
      if (selectedTab == routesListTab) {
        showRoutesList();
      } else if (selectedTab == routesFlowTab) {
        showRoutesFlow();
      } else if (selectedTab == routesAnalyticsTab) {
        showRoutesAnalytics();
      }
    });
  }

  private void showRoutesList() {
    contentArea.removeAll();
    contentArea.add(getToolbar(), getContent());
    updateList();
  }

  private void showRoutesFlow() {
    contentArea.removeAll();
    contentArea.add(createRoutesFlowVisualization());
  }

  private void showRoutesAnalytics() {
    contentArea.removeAll();
    contentArea.add(createRoutesAnalytics());
  }

  private void configureGrid() {
    grid.addClassName("routes-grid");
    grid.setSizeFull();
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(RouteInfo::routeId)
        .setHeader(translationService.getTranslation("routes.id.header"))
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(new ComponentRenderer<>(this::createMethodsBadge))
        .setHeader(translationService.getTranslation("routes.methods.header"))
        .setWidth("150px");

    grid.addColumn(RouteInfo::path)
        .setHeader(translationService.getTranslation("routes.path.header"))
        .setSortable(true)
        .setFlexGrow(2);

    grid.addColumn(RouteInfo::targetUri)
        .setHeader(translationService.getTranslation("routes.target.header"))
        .setSortable(true)
        .setFlexGrow(2);

    grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader(translationService.getTranslation("routes.status.header"))
        .setWidth("100px");

    grid.addColumn(new ComponentRenderer<>(this::createActionsColumn))
        .setHeader(translationService.getTranslation("routes.actions.header"))
        .setWidth("120px")
        .setFlexGrow(0);

    // Add details row for filters and metadata
    grid.setDetailsVisibleOnClick(false);
    grid.addItemClickListener(event -> {
      RouteInfo route = event.getItem();
      if (grid.isDetailsVisible(route)) {
        grid.setDetailsVisible(route, false);
      } else {
        grid.setDetailsVisible(route, true);
      }
    });

    grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createRouteDetails));
  }

  private Component createMethodsBadge(RouteInfo route) {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(false);
    layout.addClassName(LumoUtility.Gap.XSMALL);

    for (String method : route.methods()) {
      Span badge = new Span(method);
      badge.getElement().getThemeList().add("badge");
      switch (method.toUpperCase()) {
        case "GET" -> badge.getElement().getThemeList().add("success");
        case "POST" -> badge.getElement().getThemeList().add("primary");
        case "PUT" -> badge.getElement().getThemeList().add("warning");
        case "DELETE" -> badge.getElement().getThemeList().add("error");
        default -> badge.getElement().getThemeList().add("contrast");
      }
      layout.add(badge);
    }
    return layout;
  }

  private Component createStatusBadge(RouteInfo route) {
    Span badge = new Span(route.status());
    badge.getElement().getThemeList().add("badge");

    switch (route.status().toLowerCase()) {
      case "active":
        badge.getElement().getThemeList().add("success");
        break;
      case "inactive":
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

  private Component createActionsColumn(RouteInfo route) {
    Button testButton = new Button(new Icon(VaadinIcon.PLAY));
    testButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    testButton.setTooltipText("Test Route");
    testButton.addClickListener(e -> testRoute(route));

    Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    refreshButton.setTooltipText("Refresh Route");
    refreshButton.addClickListener(e -> refreshRoute(route));

    HorizontalLayout actions = new HorizontalLayout(testButton, refreshButton);
    actions.setSpacing(false);
    return actions;
  }

  private Component createRouteDetails(RouteInfo route) {
    VerticalLayout details = new VerticalLayout();
    details.setPadding(true);
    details.addClassNames(LumoUtility.Background.CONTRAST_5);

    // Filters section
    if (route.filters() != null && !route.filters().isEmpty()) {
      H4 filtersTitle = new H4("Applied Filters");
      VerticalLayout filtersLayout = new VerticalLayout();
      filtersLayout.setPadding(false);
      filtersLayout.setSpacing(false);

      for (String filter : route.filters()) {
        Span filterSpan = new Span("• " + filter);
        filterSpan.addClassNames(LumoUtility.FontSize.SMALL);
        filtersLayout.add(filterSpan);
      }

      details.add(filtersTitle, filtersLayout);
    }

    // Metadata section
    if (route.metadata() != null && !route.metadata().isEmpty()) {
      H4 metadataTitle = new H4("Metadata");
      VerticalLayout metadataLayout = new VerticalLayout();
      metadataLayout.setPadding(false);
      metadataLayout.setSpacing(false);

      route.metadata().forEach((key, value) -> {
        HorizontalLayout metaItem = new HorizontalLayout();
        metaItem.add(
            new Span(key + ":"),
            new Span(value)
        );
        metaItem.addClassNames(LumoUtility.FontSize.SMALL);
        metadataLayout.add(metaItem);
      });

      details.add(metadataTitle, metadataLayout);
    }

    return details;
  }

  private void configureFilters() {
    statusFilter.setLabel(translationService.getTranslation("routes.status.filter"));
    statusFilter.setItems(
        translationService.getTranslation("common.all"),
        translationService.getTranslation("common.active"),
        translationService.getTranslation("common.inactive"),
        translationService.getTranslation("common.warning")
    );
    statusFilter.setValue(translationService.getTranslation("common.all"));
    statusFilter.addValueChangeListener(e -> updateList());

    searchField.setPlaceholder(translationService.getTranslation("routes.search.placeholder"));
    searchField.setClearButtonVisible(true);
    searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
    searchField.addValueChangeListener(e -> updateList());
  }

  private HorizontalLayout getToolbar() {
    Button refreshAllButton = new Button(translationService.getTranslation("routes.refresh.all"), new Icon(VaadinIcon.REFRESH));
    refreshAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshAllButton.addClickListener(click -> refreshAllRoutes());

    Button clearCacheButton = new Button(translationService.getTranslation("routes.clear.cache"), new Icon(VaadinIcon.TRASH));
    clearCacheButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    clearCacheButton.addClickListener(click -> clearRouteCache());

    HorizontalLayout toolbar = new HorizontalLayout(statusFilter, searchField, refreshAllButton, clearCacheButton);
    toolbar.setAlignItems(FlexComponent.Alignment.END);
    toolbar.addClassName("toolbar");
    return toolbar;
  }

  private VerticalLayout getContent() {
    VerticalLayout content = new VerticalLayout(grid);
    content.addClassName("content");
    content.setSizeFull();
    content.setPadding(false);
    return content;
  }

  private void testRoute(RouteInfo route) {
    try {
      boolean testResult = routeService.testRoute(route.routeId());
      if (testResult) {
        showNotification("Route test successful", NotificationVariant.LUMO_SUCCESS);
      } else {
        showNotification("Route test failed", NotificationVariant.LUMO_ERROR);
      }
    } catch (Exception e) {
      showNotification("Error testing route: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void refreshRoute(RouteInfo route) {
    try {
      routeService.refreshRoute(route.routeId());
      updateList();
      showNotification("Route refreshed successfully", NotificationVariant.LUMO_SUCCESS);
    } catch (Exception e) {
      showNotification("Error refreshing route: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void refreshAllRoutes() {
    try {
      routeService.refreshAllRoutes();
      updateList();
      showNotification("All routes refreshed successfully", NotificationVariant.LUMO_SUCCESS);
    } catch (Exception e) {
      showNotification("Error refreshing routes: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void clearRouteCache() {
    try {
      routeService.clearCache();
      updateList();
      showNotification("Route cache cleared successfully", NotificationVariant.LUMO_SUCCESS);
    } catch (Exception e) {
      showNotification("Error clearing cache: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void updateList() {
    String searchTerm = searchField.getValue();
    String statusFilter = this.statusFilter.getValue();

    List<RouteInfo> routes = routeService.getAllRoutes();

    // Apply status filter
    if (statusFilter != null && !"All".equals(statusFilter)) {
      routes = routes.stream()
          .filter(r -> statusFilter.equalsIgnoreCase(r.status()))
          .collect(Collectors.toList());
    }

    // Apply search filter
    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      routes = routes.stream()
          .filter(r -> r.routeId().toLowerCase().contains(searchTerm.toLowerCase()) ||
              r.path().toLowerCase().contains(searchTerm.toLowerCase()) ||
              r.targetUri().toLowerCase().contains(searchTerm.toLowerCase()))
          .collect(Collectors.toList());
    }

    grid.setItems(routes);
  }

  private void showNotification(String message, NotificationVariant variant) {
    Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
    notification.addThemeVariants(variant);
  }

  private Component createRoutesFlowVisualization() {
    VerticalLayout flowLayout = new VerticalLayout();
    flowLayout.setSizeFull();
    flowLayout.setPadding(true);
    flowLayout.setSpacing(true);

    H3 title = new H3(translationService.getTranslation("routes.flow.title"));
    title.addClassName("kong-fade-in");

    // Create a visual representation of routes flow
    Div flowContainer = new Div();
    flowContainer.addClassName("routes-flow-container");
    flowContainer.getStyle()
        .set("background", "var(--kong-card-gradient)")
        .set("backdrop-filter", "blur(20px)")
        .set("border", "1px solid var(--kong-glass-border)")
        .set("border-radius", "var(--kong-radius-xl)")
        .set("padding", "var(--kong-space-6)")
        .set("min-height", "400px");

    // Add sample route flow visualization
    createRouteFlowDiagram(flowContainer);

    flowLayout.add(title, flowContainer);
    return flowLayout;
  }

  private Component createRoutesAnalytics() {
    VerticalLayout analyticsLayout = new VerticalLayout();
    analyticsLayout.setSizeFull();
    analyticsLayout.setPadding(true);
    analyticsLayout.setSpacing(true);

    H3 title = new H3(translationService.getTranslation("routes.analytics.title"));
    title.addClassName("kong-fade-in");

    HorizontalLayout chartsRow1 = new HorizontalLayout();
    chartsRow1.setSizeFull();
    chartsRow1.setSpacing(true);

    chartsRow1.add(createRoutesByMethodChart(), createRoutesByStatusChart());

    HorizontalLayout chartsRow2 = new HorizontalLayout();
    chartsRow2.setSizeFull();
    chartsRow2.setSpacing(true);

    chartsRow2.add(createTopRoutesChart(), createRoutesPerformanceChart());

    analyticsLayout.add(title, chartsRow1, chartsRow2);
    return analyticsLayout;
  }

  private void createRouteFlowDiagram(Div container) {
    // Create a simple visual representation of route flow
    List<RouteInfo> routes = routeService.getAllRoutes();

    for (RouteInfo route : routes) {
      Div routeNode = new Div();
      routeNode.addClassName("route-flow-node");
      routeNode.getStyle()
          .set("background", "var(--kong-glass-bg)")
          .set("border", "1px solid var(--kong-glass-border)")
          .set("border-radius", "var(--kong-radius-lg)")
          .set("padding", "var(--kong-space-4)")
          .set("margin", "var(--kong-space-2)")
          .set("display", "inline-block");

      Span routePath = new Span(route.path());
      routePath.getStyle().set("font-weight", "600").set("color", "var(--kong-light)");

      Span routeTarget = new Span(" → " + route.targetUri());
      routeTarget.getStyle().set("color", "rgba(255,255,255,0.7)").set("font-size", "0.9em");

      routeNode.add(routePath, routeTarget);
      container.add(routeNode);
    }
  }

  private Component createRoutesByMethodChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("routes.methods.chart"));
    Div chartContainer = createSimplePieChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createRoutesByStatusChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("routes.status.chart"));
    Div chartContainer = createSimpleColumnChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createTopRoutesChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("routes.top.chart"));
    Div chartContainer = createSimpleBarChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createRoutesPerformanceChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("routes.performance.chart"));
    Div chartContainer = createSimpleLineChart();
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
    for (int i = 0; i < 10; i++) {
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
    String points = "0,80 10,70 20,75 30,60 40,65 50,55 60,70 70,50 80,60 90,45 100,55";
    area.getStyle().set("clip-path", "polygon(" + points + ")");
    chart.add(area);

    return chart;
  }

  private Div createSimpleBarChart() {
    Div chart = new Div();
    chart.addClassName("kong-simple-chart");
    chart.addClassName("kong-bar-chart");

    String[] labels = {"Route-1", "Route-2", "Route-3", "Route-4", "Route-5"};
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

    String[] statuses = {"Active", "Inactive", "Warning"};
    for (int i = 0; i < statuses.length; i++) {
      Div column = new Div();
      column.addClassName("kong-chart-column");
      int height = 25 + (int)(Math.random() * 50);
      column.getStyle().set("height", height + "%");
      column.getStyle().set("animation-delay", (i * 0.2) + "s");

      Span label = new Span(statuses[i]);
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

    // Create pie segments for HTTP methods
    String[] segments = {"#667eea", "#764ba2", "#f093fb", "#4facfe", "#00f2fe"};
    int[] percentages = {45, 25, 15, 10, 5};
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
}