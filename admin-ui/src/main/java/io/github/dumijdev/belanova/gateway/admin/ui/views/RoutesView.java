package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RouteInfo;
import io.github.dumijdev.belanova.gateway.admin.ui.services.RouteService;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Routes")
@Route(value = "routes", layout = MainLayout.class)
public class RoutesView extends VerticalLayout {

  private final RouteService routeService;
  private final Grid<RouteInfo> grid;
  private final TextField searchField;
  private final Select<String> statusFilter;

  public RoutesView(RouteService routeService) {
    this.routeService = routeService;
    this.grid = new Grid<>(RouteInfo.class, false);
    this.searchField = new TextField();
    this.statusFilter = new Select<>();

    setSizeFull();

    configureGrid();
    configureFilters();

    add(getToolbar(), getContent());

    updateList();
  }

  private void configureGrid() {
    grid.addClassName("routes-grid");
    grid.setSizeFull();
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(RouteInfo::routeId)
        .setHeader("Route ID")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(new ComponentRenderer<>(this::createMethodsBadge))
        .setHeader("Methods")
        .setWidth("150px");

    grid.addColumn(RouteInfo::path)
        .setHeader("Path Pattern")
        .setSortable(true)
        .setFlexGrow(2);

    grid.addColumn(RouteInfo::targetUri)
        .setHeader("Target URI")
        .setSortable(true)
        .setFlexGrow(2);

    grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader("Status")
        .setWidth("100px");

    grid.addColumn(new ComponentRenderer<>(this::createActionsColumn))
        .setHeader("Actions")
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
    statusFilter.setLabel("Status");
    statusFilter.setItems("All", "Active", "Inactive", "Warning");
    statusFilter.setValue("All");
    statusFilter.addValueChangeListener(e -> updateList());

    searchField.setPlaceholder("Search routes...");
    searchField.setClearButtonVisible(true);
    searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
    searchField.addValueChangeListener(e -> updateList());
  }

  private HorizontalLayout getToolbar() {
    Button refreshAllButton = new Button("Refresh All Routes", new Icon(VaadinIcon.REFRESH));
    refreshAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshAllButton.addClickListener(click -> refreshAllRoutes());

    Button clearCacheButton = new Button("Clear Cache", new Icon(VaadinIcon.TRASH));
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
}