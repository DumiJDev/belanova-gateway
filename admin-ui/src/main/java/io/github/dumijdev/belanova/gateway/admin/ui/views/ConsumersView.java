package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.Consumer;
import io.github.dumijdev.belanova.gateway.admin.ui.services.ConsumerService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Consumers")
@Route(value = "consumers", layout = MainLayout.class)
public class ConsumersView extends VerticalLayout {

  private final ConsumerService consumerService;
  private final TranslationService translationService;
  private final Grid<Consumer> grid;
  private final TextField searchField;
  private final Select<String> statusFilter;
  private final Tabs consumersTabs;
  private final VerticalLayout contentArea;

  public ConsumersView(ConsumerService consumerService, TranslationService translationService) {
    this.consumerService = consumerService;
    this.translationService = translationService;
    this.grid = new Grid<>(Consumer.class, false);
    this.searchField = new TextField();
    this.statusFilter = new Select<>();
    this.consumersTabs = new Tabs();
    this.contentArea = new VerticalLayout();

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    // Kong-inspired styling
    getStyle().set("background", "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)");

    configureTabs();
    configureGrid();
    configureFilters();

    add(createHeader(), consumersTabs, contentArea);

    // Default to consumers list
    showConsumersList();
  }

  private void configureTabs() {
    Tab consumersListTab = new Tab(new Icon(VaadinIcon.USERS), new Span("Consumers"));
    Tab consumersAnalyticsTab = new Tab(new Icon(VaadinIcon.CHART), new Span("Analytics"));
    Tab consumersUsageTab = new Tab(new Icon(VaadinIcon.TRENDING_UP), new Span("Usage"));

    consumersTabs.add(consumersListTab, consumersAnalyticsTab, consumersUsageTab);
    consumersTabs.addClassName("kong-dashboard-tabs");

    consumersTabs.addSelectedChangeListener(event -> {
      Tab selectedTab = event.getSelectedTab();
      if (selectedTab == consumersListTab) {
        showConsumersList();
      } else if (selectedTab == consumersAnalyticsTab) {
        showConsumersAnalytics();
      } else if (selectedTab == consumersUsageTab) {
        showConsumersUsage();
      }
    });
  }

  private void showConsumersList() {
    contentArea.removeAll();
    contentArea.add(getToolbar(), getContent());
    updateList();
  }

  private void showConsumersAnalytics() {
    contentArea.removeAll();
    contentArea.add(createConsumersAnalytics());
  }

  private void showConsumersUsage() {
    contentArea.removeAll();
    contentArea.add(createConsumersUsage());
  }

  private Component createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM);

    H3 title = new H3("API Consumers");
    title.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontWeight.BOLD);

    Button addButton = new Button("Add Consumer", new Icon(VaadinIcon.PLUS));
    addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addButton.getElement().setAttribute("kong-primary", "");
    addButton.addClickListener(e -> openConsumerDialog(null));

    header.add(title, addButton);
    return header;
  }

  private void configureGrid() {
    grid.addClassName("consumers-grid");
    grid.setSizeFull();
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(Consumer::username)
        .setHeader("Username")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(Consumer::customId)
        .setHeader("Custom ID")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader("Status")
        .setWidth("100px");

    grid.addColumn(consumer -> consumer.createdAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
        .setHeader("Created")
        .setSortable(true)
        .setWidth("120px");

    grid.addColumn(new ComponentRenderer<>(this::createActionsColumn))
        .setHeader("Actions")
        .setWidth("150px")
        .setFlexGrow(0);

    // Add details row for consumer info
    grid.setDetailsVisibleOnClick(false);
    grid.addItemClickListener(event -> {
      Consumer consumer = event.getItem();
      if (grid.isDetailsVisible(consumer)) {
        grid.setDetailsVisible(consumer, false);
      } else {
        grid.setDetailsVisible(consumer, true);
      }
    });

    grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createConsumerDetails));
  }

  private Component createStatusBadge(Consumer consumer) {
    Span badge = new Span(consumer.status());
    badge.getElement().getThemeList().add("badge");

    switch (consumer.status().toLowerCase()) {
      case "active":
        badge.getElement().getThemeList().add("success");
        break;
      case "inactive":
        badge.getElement().getThemeList().add("error");
        break;
      case "pending":
        badge.getElement().getThemeList().add("warning");
        break;
      default:
        badge.getElement().getThemeList().add("contrast");
    }

    return badge;
  }

  private Component createActionsColumn(Consumer consumer) {
    Button editButton = new Button(new Icon(VaadinIcon.EDIT));
    editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    editButton.setTooltipText("Edit Consumer");
    editButton.addClickListener(e -> openConsumerDialog(consumer));

    Button credentialsButton = new Button(new Icon(VaadinIcon.KEY));
    credentialsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    credentialsButton.setTooltipText("Manage Credentials");
    credentialsButton.addClickListener(e -> openCredentialsDialog(consumer));

    Button toggleButton = new Button(new Icon(consumer.status().equals("active") ? VaadinIcon.PAUSE : VaadinIcon.PLAY));
    toggleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    toggleButton.setTooltipText(consumer.status().equals("active") ? "Deactivate" : "Activate");
    toggleButton.addClickListener(e -> toggleConsumerStatus(consumer));

    HorizontalLayout actions = new HorizontalLayout(editButton, credentialsButton, toggleButton);
    actions.setSpacing(false);
    return actions;
  }

  private Component createConsumerDetails(Consumer consumer) {
    VerticalLayout details = new VerticalLayout();
    details.setPadding(true);
    details.addClassNames("kong-form", LumoUtility.BorderRadius.LARGE);

    // Basic info
    H3 infoTitle = new H3("Consumer Information");
    infoTitle.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

    Span idSpan = new Span("ID: " + consumer.id());
    Span usernameSpan = new Span("Username: " + consumer.username());
    Span customIdSpan = new Span("Custom ID: " + (consumer.customId() != null ? consumer.customId() : "N/A"));
    Span createdSpan = new Span("Created: " + consumer.createdAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));

    VerticalLayout infoLayout = new VerticalLayout(idSpan, usernameSpan, customIdSpan, createdSpan);
    infoLayout.setPadding(false);
    infoLayout.setSpacing(false);

    details.add(infoTitle, infoLayout);
    return details;
  }

  private void configureFilters() {
    statusFilter.setLabel("Status");
    statusFilter.setItems("All", "Active", "Inactive", "Pending");
    statusFilter.setValue("All");
    statusFilter.addValueChangeListener(e -> updateList());

    searchField.setPlaceholder("Search consumers...");
    searchField.setClearButtonVisible(true);
    searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
    searchField.addValueChangeListener(e -> updateList());
  }

  private HorizontalLayout getToolbar() {
    Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshButton.addClickListener(click -> updateList());

    HorizontalLayout toolbar = new HorizontalLayout(statusFilter, searchField, refreshButton);
    toolbar.setAlignItems(FlexComponent.Alignment.END);
    toolbar.addClassName("toolbar");
    toolbar.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL);

    return toolbar;
  }

  private VerticalLayout getContent() {
    VerticalLayout content = new VerticalLayout(grid);
    content.addClassName("content");
    content.setSizeFull();
    content.setPadding(false);
    return content;
  }

  private void openConsumerDialog(Consumer consumer) {
    ConsumerDialog dialog = new ConsumerDialog(consumer, consumerService, this::updateList);
    dialog.open();
  }

  private void openCredentialsDialog(Consumer consumer) {
    CredentialsDialog dialog = new CredentialsDialog(consumer, consumerService);
    dialog.open();
  }

  private void toggleConsumerStatus(Consumer consumer) {
    try {
      String newStatus = consumer.status().equals("active") ? "inactive" : "active";
      consumerService.updateConsumerStatus(consumer.id(), newStatus);
      updateList();
      showNotification("Consumer " + newStatus, NotificationVariant.LUMO_SUCCESS);
    } catch (Exception e) {
      showNotification("Error updating consumer status: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void updateList() {
    String searchTerm = searchField.getValue();
    String statusFilter = this.statusFilter.getValue();

    List<Consumer> consumers = consumerService.getAllConsumers();

    // Apply status filter
    if (statusFilter != null && !"All".equals(statusFilter)) {
      consumers = consumers.stream()
          .filter(c -> statusFilter.equalsIgnoreCase(c.status()))
          .collect(Collectors.toList());
    }

    // Apply search filter
    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      consumers = consumers.stream()
          .filter(c -> c.username().toLowerCase().contains(searchTerm.toLowerCase()) ||
              (c.customId() != null && c.customId().toLowerCase().contains(searchTerm.toLowerCase())))
          .collect(Collectors.toList());
    }

    grid.setItems(consumers);
  }

  private void showNotification(String message, NotificationVariant variant) {
    Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
    notification.addThemeVariants(variant);
  }

  private Component createConsumersAnalytics() {
    VerticalLayout analyticsLayout = new VerticalLayout();
    analyticsLayout.setSizeFull();
    analyticsLayout.setPadding(true);
    analyticsLayout.setSpacing(true);

    H3 title = new H3("Consumer Analytics");
    title.addClassName("kong-fade-in");

    HorizontalLayout chartsRow1 = new HorizontalLayout();
    chartsRow1.setSizeFull();
    chartsRow1.setSpacing(true);

    chartsRow1.add(createConsumerStatusChart(), createConsumerGrowthChart());

    HorizontalLayout chartsRow2 = new HorizontalLayout();
    chartsRow2.setSizeFull();
    chartsRow2.setSpacing(true);

    chartsRow2.add(createTopConsumersChart(), createConsumerActivityChart());

    analyticsLayout.add(title, chartsRow1, chartsRow2);
    return analyticsLayout;
  }

  private Component createConsumersUsage() {
    VerticalLayout usageLayout = new VerticalLayout();
    usageLayout.setSizeFull();
    usageLayout.setPadding(true);
    usageLayout.setSpacing(true);

    H3 title = new H3("Consumer Usage Statistics");
    title.addClassName("kong-fade-in");

    HorizontalLayout chartsRow1 = new HorizontalLayout();
    chartsRow1.setSizeFull();
    chartsRow1.setSpacing(true);

    chartsRow1.add(createUsageByTimeChart(), createRequestsByConsumerChart());

    HorizontalLayout chartsRow2 = new HorizontalLayout();
    chartsRow2.setSizeFull();
    chartsRow2.setSpacing(true);

    chartsRow2.add(createErrorRateByConsumerChart(), createAverageResponseTimeChart());

    usageLayout.add(title, chartsRow1, chartsRow2);
    return usageLayout;
  }

  private Component createConsumerStatusChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.status.chart"));
    Div chartContainer = createSimplePieChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createConsumerGrowthChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.growth.chart"));
    Div chartContainer = createSimpleAreaChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createTopConsumersChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.top.chart"));
    Div chartContainer = createSimpleBarChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createConsumerActivityChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.activity.chart"));
    Div chartContainer = createSimpleScatterChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createUsageByTimeChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.usage.time.chart"));
    Div chartContainer = createSimpleLineChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createRequestsByConsumerChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.requests.chart"));
    Div chartContainer = createSimpleColumnChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createErrorRateByConsumerChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.error.rate.chart"));
    Div chartContainer = createSimpleBarChart();
    chartContainer.setHeight("250px");

    card.add(title, chartContainer);
    return card;
  }

  private Component createAverageResponseTimeChart() {
    VerticalLayout card = new VerticalLayout();
    card.addClassName("kong-metrics-card");
    card.setFlexGrow(1);

    H4 title = new H4(translationService.getTranslation("consumers.response.time.chart"));
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

    String[] labels = {"Consumer-1", "Consumer-2", "Consumer-3", "Consumer-4", "Consumer-5"};
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

    String[] statuses = {"Active", "Inactive", "Pending"};
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

    // Create pie segments for consumer status
    String[] segments = {"#667eea", "#764ba2", "#f093fb"};
    int[] percentages = {65, 25, 10};
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
    for (int i = 0; i < 15; i++) {
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

  // Consumer Dialog
  private static class ConsumerDialog extends Dialog {
    private final Consumer consumer;
    private final ConsumerService consumerService;
    private final Runnable onSave;
    private final TextField usernameField;
    private final TextField customIdField;

    public ConsumerDialog(Consumer consumer, ConsumerService consumerService, Runnable onSave) {
      this.consumer = consumer;
      this.consumerService = consumerService;
      this.onSave = onSave;
      this.usernameField = new TextField("Username");
      this.customIdField = new TextField("Custom ID");

      setHeaderTitle(consumer == null ? "Add Consumer" : "Edit Consumer");
      setModal(true);
      setDraggable(true);
      setResizable(true);
      setWidth("500px");

      configureForm();

      Button saveButton = new Button("Save", e -> save());
      saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
      saveButton.getElement().setAttribute("kong-primary", "");

      Button cancelButton = new Button("Cancel", e -> close());

      getFooter().add(cancelButton, saveButton);
    }

    private void configureForm() {
      FormLayout formLayout = new FormLayout();
      formLayout.addClassName("kong-form");

      usernameField.setRequired(true);
      customIdField.setHelperText("Optional unique identifier for the consumer");

      if (consumer != null) {
        usernameField.setValue(consumer.username());
        if (consumer.customId() != null) {
          customIdField.setValue(consumer.customId());
        }
      }

      formLayout.add(usernameField, customIdField);
      add(formLayout);
    }

    private void save() {
      try {
        if (consumer == null) {
          consumerService.createConsumer(usernameField.getValue(), customIdField.getValue());
        } else {
          consumerService.updateConsumer(consumer.id(), usernameField.getValue(), customIdField.getValue());
        }
        close();
        if (onSave != null) {
          onSave.run();
        }
      } catch (Exception e) {
        Notification.show("Error saving consumer: " + e.getMessage(), 3000, Notification.Position.TOP_END)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    }
  }

  // Credentials Dialog
  private static class CredentialsDialog extends Dialog {
    public CredentialsDialog(Consumer consumer, ConsumerService consumerService) {
      setHeaderTitle("Manage Credentials - " + consumer.username());
      setModal(true);
      setWidth("600px");
      setHeight("400px");

      // Placeholder for credentials management
      VerticalLayout content = new VerticalLayout();
      content.add(new Span("Credentials management feature coming soon..."));
      add(content);

      Button closeButton = new Button("Close", e -> close());
      getFooter().add(closeButton);
    }
  }
}