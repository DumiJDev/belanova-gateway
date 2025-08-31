package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.BackendService;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;
import io.github.dumijdev.belanova.gateway.common.model.Backend;

@Route(value = "backends", layout = MainLayout.class)
@PageTitle("Backend Management")
@CssImport("./styles/backends.css")
public class BackendsView extends VerticalLayout {

  private final BackendService backendService;
  private final TranslationService translationService;
  private final Grid<Backend> grid = new Grid<>(Backend.class, false);
  private final TextField searchField = new TextField();
  private final ComboBox<String> statusFilter = new ComboBox<>();

  public BackendsView(BackendService backendService, TranslationService translationService) {
    this.backendService = backendService;
    this.translationService = translationService;

    setSizeFull();
    setPadding(false);
    setSpacing(false);
    addClassName("backends-view");

    add(createHeader());
    add(createFilters());
    add(createGrid());

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

    H1 title = new H1(translationService.getTranslation("navigation.backends"));
    title.addClassName("view-title");

    Span subtitle = new Span("Manage backend services and their configurations");
    subtitle.addClassName("view-subtitle");

    titleSection.add(title, subtitle);

    // Action buttons
    HorizontalLayout actions = new HorizontalLayout();
    actions.setSpacing(true);

    Button addBtn = new Button(translationService.getTranslation("common.add"));
    addBtn.setIcon(VaadinIcon.PLUS.create());
    addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addBtn.addClassName("action-button");
    addBtn.addClickListener(e -> openEditor(new Backend(), "Add Backend"));

    Button refreshBtn = new Button(translationService.getTranslation("common.refresh"));
    refreshBtn.setIcon(VaadinIcon.REFRESH.create());
    refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshBtn.addClassName("action-button");
    refreshBtn.addClickListener(e -> loadData());

    Button exportBtn = new Button(translationService.getTranslation("common.export"));
    exportBtn.setIcon(VaadinIcon.DOWNLOAD.create());
    exportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    exportBtn.addClassName("action-button");

    actions.add(addBtn, refreshBtn, exportBtn);

    header.add(titleSection, actions);
    return header;
  }

  private Component createFilters() {
    HorizontalLayout filters = new HorizontalLayout();
    filters.addClassName("filters-section");
    filters.setWidthFull();
    filters.setAlignItems(FlexComponent.Alignment.END);

    // Search field
    searchField.setPlaceholder("Search backends...");
    searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
    searchField.addClassName("search-field");
    searchField.setValueChangeMode(ValueChangeMode.LAZY);
    searchField.addValueChangeListener(e -> applyFilters());

    // Status filter
    statusFilter.setPlaceholder("All statuses");
    statusFilter.setItems("All", "Healthy", "Unhealthy", "Disabled");
    statusFilter.setValue("All");
    statusFilter.addClassName("status-filter");
    statusFilter.addValueChangeListener(e -> applyFilters());

    // Clear filters button
    Button clearBtn = new Button("Clear filters");
    clearBtn.setIcon(VaadinIcon.CLOSE_SMALL.create());
    clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
    clearBtn.addClassName("clear-filters-btn");
    clearBtn.addClickListener(e -> clearFilters());

    filters.add(searchField, statusFilter, clearBtn);
    return filters;
  }

  private Component createGrid() {
    grid.addClassName("backends-grid");
    grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

    // Status column with visual indicator
    grid.addComponentColumn(this::createStatusIndicator)
        .setHeader("Status")
        .setWidth("120px")
        .setFlexGrow(0);

    // Name column
    grid.addColumn(Backend::getName)
        .setHeader("Name")
        .setSortable(true)
        .setWidth("200px");

    // URL/Service ID column
    grid.addComponentColumn(this::createUrlServiceColumn)
        .setHeader("Endpoint")
        .setAutoWidth(true);

    // Services count with badge
    grid.addComponentColumn(this::createServicesCountBadge)
        .setHeader("Services")
        .setWidth("100px")
        .setFlexGrow(0);

    // Upstreams count
    grid.addComponentColumn(this::createUpstreamsCountBadge)
        .setHeader("Upstreams")
        .setWidth("100px")
        .setFlexGrow(0);

    // Health status
    grid.addComponentColumn(this::createHealthIndicator)
        .setHeader("Health")
        .setWidth("120px")
        .setFlexGrow(0);

    // Actions column
    grid.addComponentColumn(this::createActionsColumn)
        .setHeader("Actions")
        .setWidth("200px")
        .setFlexGrow(0);

    grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createDetailsPanel));

    return grid;
  }

  private Component createStatusIndicator(Backend backend) {
    HorizontalLayout status = new HorizontalLayout();
    status.setAlignItems(FlexComponent.Alignment.CENTER);
    status.setSpacing(false);
    status.addClassName("status-indicator");

    Icon statusIcon = backend.isEnabled() ?
        VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CLOSE_CIRCLE.create();
    statusIcon.addClassName(backend.isEnabled() ? "status-enabled" : "status-disabled");

    Span statusText = new Span(backend.isEnabled() ?
        translationService.getTranslation("status.enabled") :
        translationService.getTranslation("status.disabled"));
    statusText.addClassName("status-text");

    status.add(statusIcon, statusText);
    return status;
  }

  private Component createUrlServiceColumn(Backend backend) {
    VerticalLayout column = new VerticalLayout();
    column.setSpacing(false);
    column.setPadding(false);

    if (backend.getBaseUrl() != null) {
      Span url = new Span(backend.getBaseUrl());
      url.addClassName("backend-url");
      column.add(url);
    }

    if (backend.getServiceId() != null) {
      Span serviceId = new Span("Service: " + backend.getServiceId());
      serviceId.addClassName("service-id");
      column.add(serviceId);
    }

    return column;
  }

  private Component createServicesCountBadge(Backend backend) {
    Span badge = new Span(String.valueOf(backend.getServices() != null ?
        backend.getServices().size() : 0));
    badge.addClassNames("count-badge", "services-badge");
    return badge;
  }

  private Component createUpstreamsCountBadge(Backend backend) {
    Span badge = new Span(String.valueOf(backend.getUpstreams() != null ?
        backend.getUpstreams().size() : 0));
    badge.addClassNames("count-badge", "upstreams-badge");
    return badge;
  }

  private Component createHealthIndicator(Backend backend) {
    HorizontalLayout health = new HorizontalLayout();
    health.setAlignItems(FlexComponent.Alignment.CENTER);
    health.setSpacing(false);
    health.addClassName("health-indicator");

    // Simulate health status
    boolean isHealthy = backend.getId() != null && backend.getId().hashCode() % 2 == 0;

    Icon healthIcon = isHealthy ?
        VaadinIcon.HEART.create() : VaadinIcon.HEART_O.create();
    healthIcon.addClassName(isHealthy ? "health-good" : "health-bad");

    Span healthText = new Span(isHealthy ?
        translationService.getTranslation("status.healthy") :
        translationService.getTranslation("status.unhealthy"));
    healthText.addClassName("health-text");

    health.add(healthIcon, healthText);
    return health;
  }

  private Component createActionsColumn(Backend backend) {
    HorizontalLayout actions = new HorizontalLayout();
    actions.setSpacing(false);
    actions.addClassName("actions-column");

    Button editBtn = new Button(VaadinIcon.EDIT.create());
    editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
    editBtn.addClassNames("action-btn", "edit-btn");
    editBtn.setTooltipText("Edit backend");
    editBtn.addClickListener(e -> openEditor(backend, "Edit Backend"));

    Button healthBtn = new Button(VaadinIcon.HEART.create());
    healthBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
    healthBtn.addClassNames("action-btn", "health-btn");
    healthBtn.setTooltipText("Check health");
    healthBtn.addClickListener(e -> checkHealth(backend));

    Button toggleBtn = new Button(backend.isEnabled() ?
        VaadinIcon.PAUSE.create() : VaadinIcon.PLAY.create());
    toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
    toggleBtn.addClassNames("action-btn", "toggle-btn");
    toggleBtn.setTooltipText(backend.isEnabled() ? "Disable" : "Enable");
    toggleBtn.addClickListener(e -> toggleBackend(backend));

    Button deleteBtn = new Button(VaadinIcon.TRASH.create());
    deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
    deleteBtn.addClassNames("action-btn", "delete-btn");
    deleteBtn.setTooltipText("Delete backend");
    deleteBtn.addClickListener(e -> confirmDelete(backend));

    actions.add(editBtn, healthBtn, toggleBtn, deleteBtn);
    return actions;
  }

  private Component createDetailsPanel(Backend backend) {
    VerticalLayout details = new VerticalLayout();
    details.addClassName("details-panel");
    details.setSpacing(false);

    // Basic info
    HorizontalLayout basicInfo = new HorizontalLayout();
    basicInfo.addClassName("detail-section");

    Div idInfo = new Div();
    idInfo.addClassName("detail-item");
    idInfo.add(new Span("ID: "), new Span(backend.getId()));

    Div descInfo = new Div();
    descInfo.addClassName("detail-item");
    descInfo.add(new Span("Description: "), new Span(backend.getDescription() != null ?
        backend.getDescription() : "No description"));

    basicInfo.add(idInfo, descInfo);

    // Health check info
    if (backend.getHealthCheck() != null) {
      Div healthInfo = new Div();
      healthInfo.addClassName("detail-item");
      healthInfo.add(new Span("Health Check: "),
          new Span(backend.getHealthCheck().toString()));
      basicInfo.add(healthInfo);
    }

    details.add(basicInfo);
    return details;
  }

  private void checkHealth(Backend backend) {
    Notification notification = Notification.show(
        "Health check triggered for " + backend.getName(),
        3000,
        Notification.Position.TOP_END
    );
    notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
  }

  private void toggleBackend(Backend backend) {
    backend.setEnabled(!backend.isEnabled());
    backendService.save(backend);
    loadData();

    String action = backend.isEnabled() ? "enabled" : "disabled";
    Notification notification = Notification.show(
        backend.getName() + " has been " + action,
        3000,
        Notification.Position.TOP_END
    );
    notification.addThemeVariants(backend.isEnabled() ?
        NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_CONTRAST);
  }

  private void confirmDelete(Backend backend) {
    ConfirmDialog dialog = new ConfirmDialog();
    dialog.setHeader("Delete Backend");
    dialog.setText("Are you sure you want to delete '" + backend.getName() +
        "'? This action cannot be undone.");
    dialog.setCancelable(true);
    dialog.setConfirmText("Delete");
    dialog.setConfirmButtonTheme("error primary");

    dialog.addConfirmListener(e -> {
      backendService.delete(backend.getId());
      loadData();
      Notification.show("Backend '" + backend.getName() + "' deleted successfully")
          .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    dialog.open();
  }

  private void openEditor(Backend backend, String title) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle(title);
    dialog.setWidth("600px");
    dialog.setMaxWidth("90vw");
    dialog.addClassName("backend-editor-dialog");

    VerticalLayout content = new VerticalLayout();
    content.setPadding(false);
    content.setSpacing(true);

    // Create form
    FormLayout form = new FormLayout();
    form.addClassName("backend-form");

    // Form fields
    TextField nameField = new TextField("Name");
    nameField.setPlaceholder("Enter backend name");
    nameField.setRequiredIndicatorVisible(true);

    TextArea descField = new TextArea("Description");
    descField.setPlaceholder("Optional description");

    TextField baseUrlField = new TextField("Base URL");
    baseUrlField.setPlaceholder("http://localhost:8080");

    TextField serviceIdField = new TextField("Service ID");
    serviceIdField.setPlaceholder("user-service");

    TextField generalPathField = new TextField("General Path");
    generalPathField.setPlaceholder("/api/v1");

    Checkbox enabledField = new Checkbox("Enabled");
    Checkbox useServiceDiscoveryField = new Checkbox("Use Service Discovery");

    // Bind fields
    Binder<Backend> binder = new Binder<>(Backend.class);
    binder.forField(nameField)
        .asRequired("Name is required")
        .bind(Backend::getName, Backend::setName);
    binder.bind(descField, Backend::getDescription, Backend::setDescription);
    binder.bind(baseUrlField, Backend::getBaseUrl, Backend::setBaseUrl);
    binder.bind(serviceIdField, Backend::getServiceId, Backend::setServiceId);
    binder.bind(generalPathField, Backend::getGeneralPath, Backend::setGeneralPath);
    binder.bind(enabledField, Backend::isEnabled, Backend::setEnabled);
    binder.bind(useServiceDiscoveryField, Backend::isUseServiceDiscovery, Backend::setUseServiceDiscovery);

    binder.readBean(backend);

    // Add fields to form
    form.add(nameField, descField, baseUrlField, serviceIdField,
        generalPathField, enabledField, useServiceDiscoveryField);
    form.setColspan(descField, 2);
    form.setColspan(baseUrlField, 2);

    content.add(form);

    // Buttons
    HorizontalLayout buttons = new HorizontalLayout();
    buttons.addClassName("dialog-buttons");
    buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

    Button cancelBtn = new Button("Cancel");
    cancelBtn.addClickListener(e -> dialog.close());

    Button saveBtn = new Button("Save");
    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveBtn.addClickListener(e -> {
      if (binder.validate().isOk()) {
        try {
          binder.writeBean(backend);
          backendService.save(backend);
          dialog.close();
          loadData();
          Notification.show("Backend saved successfully")
              .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
          Notification.show("Error saving backend: " + ex.getMessage())
              .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
      }
    });

    buttons.add(cancelBtn, saveBtn);
    content.add(buttons);

    dialog.add(content);
    dialog.open();
  }

  private void loadData() {
    grid.setItems(backendService.findAll());
  }

  private void applyFilters() {
    // Implementation for filtering would go here
    // For now, just reload data
    loadData();
  }

  private void clearFilters() {
    searchField.clear();
    statusFilter.setValue("All");
    applyFilters();
  }
}