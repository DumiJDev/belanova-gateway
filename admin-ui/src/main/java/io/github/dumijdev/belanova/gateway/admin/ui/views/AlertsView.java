package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.Alert;
import io.github.dumijdev.belanova.gateway.admin.ui.services.AlertService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Alerts")
@Route(value = "alerts", layout = MainLayout.class)
public class AlertsView extends VerticalLayout {

  private final AlertService alertService;
  private final Grid<Alert> grid;
  private final Select<String> statusFilter;
  private final Select<String> severityFilter;

  public AlertsView(AlertService alertService) {
    this.alertService = alertService;
    this.grid = new Grid<>(Alert.class, false);
    this.statusFilter = new Select<>();
    this.severityFilter = new Select<>();

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    // Kong-inspired styling
    getStyle().set("background", "linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)");

    configureGrid();
    configureFilters();

    add(createHeader(), getToolbar(), getContent());

    updateList();
  }

  private Component createHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM);

    H3 title = new H3("Alert Management");
    title.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.FontWeight.BOLD);

    Button createAlertButton = new Button("Create Alert", new Icon(VaadinIcon.PLUS));
    createAlertButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    createAlertButton.getElement().setAttribute("kong-primary", "");
    createAlertButton.addClickListener(e -> openAlertDialog(null));

    header.add(title, createAlertButton);
    return header;
  }

  private void configureGrid() {
    grid.addClassName("alerts-grid");
    grid.setSizeFull();
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(Alert::title)
        .setHeader("Alert Title")
        .setSortable(true)
        .setFlexGrow(2);

    grid.addColumn(new ComponentRenderer<>(this::createSeverityBadge))
        .setHeader("Severity")
        .setWidth("100px");

    grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader("Status")
        .setWidth("100px");

    grid.addColumn(alert -> alert.createdAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")))
        .setHeader("Created")
        .setSortable(true)
        .setWidth("120px");

    grid.addColumn(Alert::source)
        .setHeader("Source")
        .setSortable(true)
        .setWidth("120px");

    grid.addColumn(new ComponentRenderer<>(this::createActionsColumn))
        .setHeader("Actions")
        .setWidth("150px")
        .setFlexGrow(0);

    // Add details row for alert description
    grid.setDetailsVisibleOnClick(false);
    grid.addItemClickListener(event -> {
      Alert alert = event.getItem();
      if (grid.isDetailsVisible(alert)) {
        grid.setDetailsVisible(alert, false);
      } else {
        grid.setDetailsVisible(alert, true);
      }
    });

    grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createAlertDetails));
  }

  private Component createSeverityBadge(Alert alert) {
    Span badge = new Span(alert.severity());
    badge.getElement().getThemeList().add("badge");

    switch (alert.severity().toLowerCase()) {
      case "critical":
        badge.getElement().getThemeList().add("error");
        break;
      case "high":
        badge.getElement().getThemeList().add("warning");
        break;
      case "medium":
        badge.getElement().getThemeList().add("primary");
        break;
      case "low":
        badge.getElement().getThemeList().add("success");
        break;
      default:
        badge.getElement().getThemeList().add("contrast");
    }

    return badge;
  }

  private Component createStatusBadge(Alert alert) {
    Span badge = new Span(alert.status());
    badge.getElement().getThemeList().add("badge");

    switch (alert.status().toLowerCase()) {
      case "active":
        badge.getElement().getThemeList().add("error");
        break;
      case "acknowledged":
        badge.getElement().getThemeList().add("warning");
        break;
      case "resolved":
        badge.getElement().getThemeList().add("success");
        break;
      default:
        badge.getElement().getThemeList().add("contrast");
    }

    return badge;
  }

  private Component createActionsColumn(Alert alert) {
    Button acknowledgeButton = new Button(new Icon(VaadinIcon.CHECK));
    acknowledgeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    acknowledgeButton.setTooltipText("Acknowledge Alert");
    acknowledgeButton.addClickListener(e -> acknowledgeAlert(alert));
    acknowledgeButton.setEnabled(!"acknowledged".equals(alert.status()) && !"resolved".equals(alert.status()));

    Button resolveButton = new Button(new Icon(VaadinIcon.CLOSE));
    resolveButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    resolveButton.setTooltipText("Resolve Alert");
    resolveButton.addClickListener(e -> resolveAlert(alert));
    resolveButton.setEnabled(!"resolved".equals(alert.status()));

    Button editButton = new Button(new Icon(VaadinIcon.EDIT));
    editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    editButton.setTooltipText("Edit Alert");
    editButton.addClickListener(e -> openAlertDialog(alert));

    HorizontalLayout actions = new HorizontalLayout(acknowledgeButton, resolveButton, editButton);
    actions.setSpacing(false);
    return actions;
  }

  private Component createAlertDetails(Alert alert) {
    VerticalLayout details = new VerticalLayout();
    details.setPadding(true);
    details.addClassNames("kong-form", LumoUtility.BorderRadius.LARGE);

    H3 descriptionTitle = new H3("Description");
    descriptionTitle.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

    Span description = new Span(alert.description());
    description.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST);

    details.add(descriptionTitle, description);

    if (alert.resolution() != null && !alert.resolution().isEmpty()) {
      H3 resolutionTitle = new H3("Resolution");
      resolutionTitle.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

      Span resolution = new Span(alert.resolution());
      resolution.addClassNames(LumoUtility.TextColor.PRIMARY_CONTRAST);

      details.add(resolutionTitle, resolution);
    }

    return details;
  }

  private void configureFilters() {
    statusFilter.setLabel("Status");
    statusFilter.setItems("All", "Active", "Acknowledged", "Resolved");
    statusFilter.setValue("All");
    statusFilter.addValueChangeListener(e -> updateList());

    severityFilter.setLabel("Severity");
    severityFilter.setItems("All", "Critical", "High", "Medium", "Low");
    severityFilter.setValue("All");
    severityFilter.addValueChangeListener(e -> updateList());
  }

  private HorizontalLayout getToolbar() {
    Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    refreshButton.addClickListener(click -> updateList());

    Button clearResolvedButton = new Button("Clear Resolved", new Icon(VaadinIcon.TRASH));
    clearResolvedButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    clearResolvedButton.addClickListener(click -> clearResolvedAlerts());

    HorizontalLayout toolbar = new HorizontalLayout(statusFilter, severityFilter, refreshButton, clearResolvedButton);
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

  private void openAlertDialog(Alert alert) {
    AlertDialog dialog = new AlertDialog(alert, alertService, this::updateList);
    dialog.open();
  }

  private void acknowledgeAlert(Alert alert) {
    try {
      alertService.updateAlertStatus(alert.id(), "acknowledged");
      updateList();
      showNotification("Alert acknowledged", NotificationVariant.LUMO_SUCCESS);
    } catch (Exception e) {
      showNotification("Error acknowledging alert: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void resolveAlert(Alert alert) {
    try {
      alertService.updateAlertStatus(alert.id(), "resolved");
      updateList();
      showNotification("Alert resolved", NotificationVariant.LUMO_SUCCESS);
    } catch (Exception e) {
      showNotification("Error resolving alert: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void clearResolvedAlerts() {
    try {
      alertService.clearResolvedAlerts();
      updateList();
      showNotification("Resolved alerts cleared", NotificationVariant.LUMO_SUCCESS);
    } catch (Exception e) {
      showNotification("Error clearing resolved alerts: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void updateList() {
    String statusFilterValue = statusFilter.getValue();
    String severityFilterValue = severityFilter.getValue();

    List<Alert> alerts = alertService.getAllAlerts();

    // Apply status filter
    if (statusFilterValue != null && !"All".equals(statusFilterValue)) {
      alerts = alerts.stream()
          .filter(alert -> statusFilterValue.equalsIgnoreCase(alert.status()))
          .collect(Collectors.toList());
    }

    // Apply severity filter
    if (severityFilterValue != null && !"All".equals(severityFilterValue)) {
      alerts = alerts.stream()
          .filter(alert -> severityFilterValue.equalsIgnoreCase(alert.severity()))
          .collect(Collectors.toList());
    }

    grid.setItems(alerts);
  }

  private void showNotification(String message, NotificationVariant variant) {
    Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
    notification.addThemeVariants(variant);
  }

  // Alert Dialog
  private static class AlertDialog extends Dialog {
    private final Alert alert;
    private final AlertService alertService;
    private final Runnable onSave;
    private final TextField titleField;
    private final Select<String> severityField;
    private final TextArea descriptionField;
    private final TextField sourceField;

    public AlertDialog(Alert alert, AlertService alertService, Runnable onSave) {
      this.alert = alert;
      this.alertService = alertService;
      this.onSave = onSave;
      this.titleField = new TextField("Title");
      this.severityField = new Select<>();
      this.descriptionField = new TextArea("Description");
      this.sourceField = new TextField("Source");

      setHeaderTitle(alert == null ? "Create Alert" : "Edit Alert");
      setModal(true);
      setDraggable(true);
      setResizable(true);
      setWidth("600px");

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

      titleField.setRequired(true);
      descriptionField.setRequired(true);

      severityField.setLabel("Severity");
      severityField.setItems("Low", "Medium", "High", "Critical");
      severityField.setValue("Medium");

      sourceField.setPlaceholder("e.g., gateway-service, backend-1");

      if (alert != null) {
        titleField.setValue(alert.title());
        severityField.setValue(alert.severity());
        descriptionField.setValue(alert.description());
        sourceField.setValue(alert.source());
      }

      formLayout.add(titleField, severityField, sourceField, descriptionField);
      add(formLayout);
    }

    private void save() {
      try {
        if (alert == null) {
          alertService.createAlert(
              titleField.getValue(),
              descriptionField.getValue(),
              severityField.getValue(),
              sourceField.getValue()
          );
        } else {
          alertService.updateAlert(
              alert.id(),
              titleField.getValue(),
              descriptionField.getValue(),
              severityField.getValue(),
              sourceField.getValue()
          );
        }
        close();
        if (onSave != null) {
          onSave.run();
        }
      } catch (Exception e) {
        Notification.show("Error saving alert: " + e.getMessage(), 3000, Notification.Position.TOP_END)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    }
  }
}