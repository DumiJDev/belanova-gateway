package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.BackendService;
import io.github.dumijdev.belanova.gateway.admin.ui.services.ServiceService;
import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumijdev.belanova.gateway.common.model.Service;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Services")
@Route(value = "services", layout = MainLayout.class)
public class ServicesView extends VerticalLayout implements HasUrlParameter<String> {

  private final ServiceService serviceService;
  private final BackendService backendService;
  private final Grid<Service> grid;
  private final Select<Backend> backendSelect;
  private final TextField searchField;
  private Backend selectedBackend;

  public ServicesView(ServiceService serviceService, BackendService backendService) {
    this.serviceService = serviceService;
    this.backendService = backendService;
    this.grid = new Grid<>(Service.class, false);
    this.backendSelect = new Select<>();
    this.searchField = new TextField();

    setSizeFull();

    configureGrid();
    configureFilters();

    add(getToolbar(), getContent());
  }

  @Override
  public void setParameter(BeforeEvent event, @OptionalParameter String backendId) {
    if (backendId != null) {
      Backend backend = backendService.findById(backendId);
      if (backend != null) {
        selectedBackend = backend;
        backendSelect.setValue(backend);
        updateList();
      }
    } else {
      updateList();
    }
  }

  private void configureGrid() {
    grid.addClassName("services-grid");
    grid.setSizeFull();
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(Service::getName)
        .setHeader("Service Name")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(Service::getPath)
        .setHeader("Path")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(service -> service.getBackend().getName())
        .setHeader("Backend")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(new ComponentRenderer<>(this::createMethodsBadges))
        .setHeader("HTTP Methods")
        .setFlexGrow(1);

    grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader("Status")
        .setWidth("120px");

    grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
        .setHeader("Actions")
        .setWidth("150px")
        .setFlexGrow(0);
  }

  private Component createMethodsBadges(Service service) {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSpacing(false);
    layout.addClassName(LumoUtility.Gap.XSMALL);

    if (service.getMethods() != null) {
      for (String method : service.getMethods()) {
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
    }
    return layout;
  }

  private Component createStatusBadge(Service service) {
    Span badge = new Span();
    if (service.isEnabled()) {
      badge.setText("Enabled");
      badge.getElement().getThemeList().add("badge success");
    } else {
      badge.setText("Disabled");
      badge.getElement().getThemeList().add("badge error");
    }
    return badge;
  }

  private Component createActionButtons(Service service) {
    Button editButton = new Button(new Icon(VaadinIcon.EDIT));
    editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    editButton.setTooltipText("Edit Service");
    editButton.addClickListener(e -> openEditDialog(service));

    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    deleteButton.setTooltipText("Delete Service");
    deleteButton.addClickListener(e -> confirmDelete(service));

    HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
    actions.setSpacing(false);
    return actions;
  }

  private void configureFilters() {
    backendSelect.setLabel("Filter by Backend");
    backendSelect.setItemLabelGenerator(Backend::getName);
    backendSelect.setItems(backendService.findAll());
    backendSelect.addValueChangeListener(e -> {
      selectedBackend = e.getValue();
      updateList();
    });

    searchField.setPlaceholder("Search services...");
    searchField.setClearButtonVisible(true);
    searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
    searchField.addValueChangeListener(e -> updateList());
  }

  private HorizontalLayout getToolbar() {
    Button addButton = new Button("Add Service", new Icon(VaadinIcon.PLUS));
    addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addButton.addClickListener(click -> openEditDialog(new Service()));

    Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshButton.setTooltipText("Refresh");
    refreshButton.addClickListener(click -> updateList());

    HorizontalLayout toolbar = new HorizontalLayout(backendSelect, searchField, addButton, refreshButton);
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

  private void openEditDialog(Service service) {
    ServiceForm form = new ServiceForm(backendService.findAll());
    Dialog dialog = new Dialog(form);

    dialog.setHeaderTitle(service.getId() == null ? "Add Service" : "Edit Service");
    dialog.setModal(true);
    dialog.setDraggable(true);
    dialog.setResizable(true);
    dialog.setWidth("600px");

    // Set default backend if one is selected
    if (service.getId() == null && selectedBackend != null) {
      service.setBackend(selectedBackend);
    }

    form.setService(service);

    Button saveButton = new Button("Save", e -> {
      try {
        Service savedService = form.save();
        serviceService.save(savedService);
        updateList();
        dialog.close();
        showNotification("Service saved successfully", NotificationVariant.LUMO_SUCCESS);
      } catch (ValidationException ex) {
        showNotification("Please fix validation errors", NotificationVariant.LUMO_ERROR);
      } catch (Exception ex) {
        showNotification("Error saving service: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    });
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button cancelButton = new Button("Cancel", e -> dialog.close());

    dialog.getFooter().add(cancelButton, saveButton);
    dialog.open();
  }

  private void confirmDelete(Service service) {
    ConfirmDialog dialog = new ConfirmDialog();
    dialog.setHeader("Delete Service");
    dialog.setText("Are you sure you want to delete service '" + service.getName() + "'?");
    dialog.setCancelable(true);
    dialog.setConfirmText("Delete");
    dialog.setConfirmButtonTheme("error primary");

    dialog.addConfirmListener(e -> {
      try {
        serviceService.delete(service.getId());
        updateList();
        showNotification("Service deleted successfully", NotificationVariant.LUMO_SUCCESS);
      } catch (Exception ex) {
        showNotification("Error deleting service: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    });

    dialog.open();
  }

  private void updateList() {
    String searchTerm = searchField.getValue();
    List<Service> services;

    if (selectedBackend != null) {
      services = serviceService.findByBackend(selectedBackend);
    } else {
      services = serviceService.findAll();
    }

    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      services = services.stream()
          .filter(s -> s.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
              s.getPath().toLowerCase().contains(searchTerm.toLowerCase()))
          .collect(Collectors.toList());
    }

    grid.setItems(services);
  }

  private void showNotification(String message, NotificationVariant variant) {
    Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
    notification.addThemeVariants(variant);
  }

  // Service Form Inner Class
  private static class ServiceForm extends VerticalLayout {
    private final Binder<Service> binder = new Binder<>(Service.class);
    private final TextField name = new TextField("Service Name");
    private final TextField path = new TextField("Path");
    private final Select<Backend> backendSelect = new Select<>();
    private final MultiSelectComboBox<String> methodsSelect = new MultiSelectComboBox<>("HTTP Methods");
    private final Checkbox enabled = new Checkbox("Enabled");
    private Service service;

    public ServiceForm(List<Backend> backends) {
      setSpacing(false);

      configureComponents(backends);

      FormLayout formLayout = new FormLayout();
      formLayout.add(name, path, backendSelect, methodsSelect, enabled);
      formLayout.setResponsiveSteps(
          new FormLayout.ResponsiveStep("0", 1),
          new FormLayout.ResponsiveStep("500px", 2)
      );

      add(formLayout);

      configureBinder();
    }

    private void configureComponents(List<Backend> backends) {
      backendSelect.setLabel("Backend");
      backendSelect.setItemLabelGenerator(Backend::getName);
      backendSelect.setItems(backends.stream().filter(Backend::isEnabled).collect(Collectors.toList()));

      methodsSelect.setItems("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");
      methodsSelect.setValue(Set.of("GET")); // Default to GET

      path.setHelperText("e.g., /api/users, /orders/**");
      path.setPrefixComponent(VaadinIcon.ROAD.create());
    }

    private void configureBinder() {
      binder.forField(name)
          .withValidator(n -> n != null && !n.trim().isEmpty(), "Service name is required")
          .bind(Service::getName, Service::setName);

      binder.forField(path)
          .withValidator(p -> p != null && !p.trim().isEmpty(), "Path is required")
          .withValidator(p -> p.startsWith("/"), "Path must start with /")
          .bind(Service::getPath, Service::setPath);

      binder.forField(backendSelect)
          .withValidator(Objects::nonNull, "Backend is required")
          .bind(Service::getBackend, Service::setBackend);

      binder.forField(methodsSelect)
          .withConverter(
              Set::copyOf,
              methods -> methods != null ? Set.copyOf(methods) : Set.of()
          )
          .bind(service1 -> new HashSet<String>(service1.getMethods()),
              (service1, strings) -> {
                service1.setMethods(new ArrayList<>(strings));
              });

      binder.forField(enabled)
          .bind(Service::isEnabled, Service::setEnabled);
    }

    public void setService(Service service) {
      this.service = service;
      if (service.getId() == null) {
        service.setEnabled(true);
        service.setMethods(List.of("GET"));
      }
      binder.setBean(service);
    }

    public Service save() throws ValidationException {
      binder.writeBean(service);
      return service;
    }
  }
}