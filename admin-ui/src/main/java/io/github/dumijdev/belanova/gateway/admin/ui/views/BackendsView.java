package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.BackendService;
import io.github.dumijdev.belanova.gateway.common.model.Backend;

@PageTitle("Backends")
@Route(value = "backends", layout = MainLayout.class)
public class BackendsView extends VerticalLayout {

  private final BackendService backendService;
  private final Grid<Backend> grid;
  private final TextField searchField;
  private Backend selectedBackend;

  public BackendsView(BackendService backendService) {
    this.backendService = backendService;
    this.grid = new Grid<>(Backend.class, false);
    this.searchField = new TextField();

    setSizeFull();

    configureGrid();
    configureToolbar();

    add(getToolbar(), getContent());

    updateList();
  }

  private void configureGrid() {
    grid.addClassName("backends-grid");
    grid.setSizeFull();
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(Backend::getName)
        .setHeader("Name")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(Backend::getDescription)
        .setHeader("Description")
        .setFlexGrow(2);

    grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader("Status")
        .setWidth("120px");

    grid.addColumn(Backend::getBaseUrl)
        .setHeader("Base URL")
        .setFlexGrow(1);

    grid.addColumn(new ComponentRenderer<>(this::createServiceDiscoveryBadge))
        .setHeader("Service Discovery")
        .setWidth("150px");

    grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
        .setHeader("Actions")
        .setWidth("200px")
        .setFlexGrow(0);

    grid.asSingleSelect().addValueChangeListener(event ->
        selectedBackend = event.getValue());
  }

  private Component createStatusBadge(Backend backend) {
    Span badge = new Span();
    if (backend.isEnabled()) {
      badge.setText("Enabled");
      badge.getElement().getThemeList().add("badge success");
    } else {
      badge.setText("Disabled");
      badge.getElement().getThemeList().add("badge error");
    }
    return badge;
  }

  private Component createServiceDiscoveryBadge(Backend backend) {
    Span badge = new Span();
    if (backend.isUseServiceDiscovery()) {
      badge.setText("Enabled");
      badge.getElement().getThemeList().add("badge primary");
    } else {
      badge.setText("Direct URL");
      badge.getElement().getThemeList().add("badge contrast");
    }
    return badge;
  }

  private Component createActionButtons(Backend backend) {
    Button editButton = new Button(new Icon(VaadinIcon.EDIT));
    editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    editButton.setTooltipText("Edit Backend");
    editButton.addClickListener(e -> openEditDialog(backend));

    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    deleteButton.setTooltipText("Delete Backend");
    deleteButton.addClickListener(e -> confirmDelete(backend));

    Button servicesButton = new Button("Services", new Icon(VaadinIcon.COG));
    servicesButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    servicesButton.addClickListener(e -> navigateToServices(backend));

    HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton, servicesButton);
    actions.setSpacing(false);
    return actions;
  }

  private void configureToolbar() {
    searchField.setPlaceholder("Search backends...");
    searchField.setClearButtonVisible(true);
    searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
    searchField.addValueChangeListener(e -> updateList());
    searchField.addClassNames(LumoUtility.Width.MEDIUM);
  }

  private HorizontalLayout getToolbar() {
    Button addButton = new Button("Add Backend", new Icon(VaadinIcon.PLUS));
    addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addButton.addClickListener(click -> openEditDialog(new Backend()));

    Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshButton.setTooltipText("Refresh");
    refreshButton.addClickListener(click -> updateList());

    HorizontalLayout toolbar = new HorizontalLayout(searchField, addButton, refreshButton);
    toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
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

  private void openEditDialog(Backend backend) {
    BackendForm form = new BackendForm();
    Dialog dialog = new Dialog(form);

    dialog.setHeaderTitle(backend.getId() == null ? "Add Backend" : "Edit Backend");
    dialog.setModal(true);
    dialog.setDraggable(true);
    dialog.setResizable(true);
    dialog.setWidth("600px");

    form.setBackend(backend);

    Button saveButton = new Button("Save", e -> {
      try {
        Backend savedBackend = form.save();
        backendService.save(savedBackend);
        updateList();
        dialog.close();
        showNotification("Backend saved successfully", NotificationVariant.LUMO_SUCCESS);
      } catch (ValidationException ex) {
        showNotification("Please fix validation errors", NotificationVariant.LUMO_ERROR);
      } catch (Exception ex) {
        showNotification("Error saving backend: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    });
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button cancelButton = new Button("Cancel", e -> dialog.close());

    dialog.getFooter().add(cancelButton, saveButton);
    dialog.open();
  }

  private void confirmDelete(Backend backend) {
    ConfirmDialog dialog = new ConfirmDialog();
    dialog.setHeader("Delete Backend");
    dialog.setText("Are you sure you want to delete backend '" + backend.getName() + "'?");
    dialog.setCancelable(true);
    dialog.setConfirmText("Delete");
    dialog.setConfirmButtonTheme("error primary");

    dialog.addConfirmListener(e -> {
      try {
        backendService.delete(backend.getId());
        updateList();
        showNotification("Backend deleted successfully", NotificationVariant.LUMO_SUCCESS);
      } catch (Exception ex) {
        showNotification("Error deleting backend: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    });

    dialog.open();
  }

  private void navigateToServices(Backend backend) {
    UI.getCurrent().navigate("services/" + backend.getId());
  }

  private void updateList() {
    String searchTerm = searchField.getValue();
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      grid.setItems(backendService.findAll());
    } else {
      grid.setItems(backendService.findByNameContaining(searchTerm));
    }
  }

  private void showNotification(String message, NotificationVariant variant) {
    Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
    notification.addThemeVariants(variant);
  }

  // Backend Form Inner Class
  private static class BackendForm extends VerticalLayout {
    private final Binder<Backend> binder = new Binder<>(Backend.class);
    private final TextField name = new TextField("Name");
    private final TextField description = new TextField("Description");
    private final TextField baseUrl = new TextField("Base URL");
    private final TextField serviceId = new TextField("Service ID");
    private final TextField generalPath = new TextField("General Path");
    private final Checkbox enabled = new Checkbox("Enabled");
    private final Checkbox useServiceDiscovery = new Checkbox("Use Service Discovery");
    private Backend backend;

    public BackendForm() {
      setSpacing(false);

      FormLayout formLayout = new FormLayout();
      formLayout.add(name, description, baseUrl, serviceId, generalPath, enabled, useServiceDiscovery);
      formLayout.setResponsiveSteps(
          new FormLayout.ResponsiveStep("0", 1),
          new FormLayout.ResponsiveStep("500px", 2)
      );

      add(formLayout);

      configureBinder();
    }

    private void configureBinder() {
      binder.forField(name)
          .withValidator(n -> n != null && !n.trim().isEmpty(), "Name is required")
          .bind(Backend::getName, Backend::setName);

      binder.forField(description)
          .bind(Backend::getDescription, Backend::setDescription);

      binder.forField(baseUrl)
          .withValidator(url -> !useServiceDiscovery.getValue() || (url != null && !url.trim().isEmpty()),
              "Base URL is required when not using service discovery")
          .bind(Backend::getBaseUrl, Backend::setBaseUrl);

      binder.forField(serviceId)
          .withValidator(id -> useServiceDiscovery.getValue() || (id != null && !id.trim().isEmpty()),
              "Service ID is required when using service discovery")
          .bind(Backend::getServiceId, Backend::setServiceId);

      binder.forField(generalPath)
          .bind(Backend::getGeneralPath, Backend::setGeneralPath);

      binder.forField(enabled)
          .bind(Backend::isEnabled, Backend::setEnabled);

      binder.forField(useServiceDiscovery)
          .bind(Backend::isUseServiceDiscovery, Backend::setUseServiceDiscovery);

      useServiceDiscovery.addValueChangeListener(e -> {
        boolean useSD = e.getValue();
        baseUrl.setEnabled(!useSD);
        serviceId.setEnabled(useSD);
      });
    }

    public void setBackend(Backend backend) {
      this.backend = backend;
      if (backend.getId() == null) {
        // New backend defaults
        backend.setEnabled(true);
        backend.setUseServiceDiscovery(false);
      }
      binder.setBean(backend);
    }

    public Backend save() throws ValidationException {
      binder.writeBean(backend);
      return backend;
    }
  }
}