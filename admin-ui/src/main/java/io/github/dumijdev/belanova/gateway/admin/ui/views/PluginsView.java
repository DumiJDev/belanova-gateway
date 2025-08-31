package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.PluginInfo;
import io.github.dumijdev.belanova.gateway.admin.ui.services.PluginService;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Plugins")
@Route(value = "plugins", layout = MainLayout.class)
public class PluginsView extends VerticalLayout {

  private final PluginService pluginService;
  private final Grid<PluginInfo> grid;
  private final TextField searchField;
  private final Select<String> phaseFilter;

  public PluginsView(PluginService pluginService) {
    this.pluginService = pluginService;
    this.grid = new Grid<>(PluginInfo.class, false);
    this.searchField = new TextField();
    this.phaseFilter = new Select<>();

    setSizeFull();

    configureGrid();
    configureFilters();

    add(getToolbar(), getContent());

    updateList();
  }

  private void configureGrid() {
    grid.addClassName("plugins-grid");
    grid.setSizeFull();
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

    grid.addColumn(PluginInfo::name)
        .setHeader("Plugin Name")
        .setSortable(true)
        .setFlexGrow(1);

    grid.addColumn(new ComponentRenderer<>(this::createPhaseBadge))
        .setHeader("Phase")
        .setWidth("120px");

    grid.addColumn(PluginInfo::order)
        .setHeader("Order")
        .setSortable(true)
        .setWidth("80px");

    grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
        .setHeader("Status")
        .setWidth("100px");

    grid.addColumn(PluginInfo::description)
        .setHeader("Description")
        .setFlexGrow(2);

    grid.addColumn(new ComponentRenderer<>(this::createActionsColumn))
        .setHeader("Actions")
        .setWidth("150px")
        .setFlexGrow(0);

    // Add details row for configuration
    grid.setDetailsVisibleOnClick(false);
    grid.addItemClickListener(event -> {
      PluginInfo plugin = event.getItem();
      if (grid.isDetailsVisible(plugin)) {
        grid.setDetailsVisible(plugin, false);
      } else {
        grid.setDetailsVisible(plugin, true);
      }
    });

    grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createPluginDetails));
  }

  private Component createPhaseBadge(PluginInfo plugin) {
    Span badge = new Span(plugin.phase().name());
    badge.getElement().getThemeList().add("badge");

    switch (plugin.phase()) {
      case PRE_AUTH -> badge.getElement().getThemeList().add("primary");
      case AUTH -> badge.getElement().getThemeList().add("success");
      case POST_AUTH -> badge.getElement().getThemeList().add("warning");
      case PRE_REQUEST -> badge.getElement().getThemeList().add("contrast");
      case POST_REQUEST -> badge.getElement().getThemeList().add("error");
      default -> badge.getElement().getThemeList().add("contrast");
    }

    return badge;
  }

  private Component createStatusBadge(PluginInfo plugin) {
    Span badge = new Span();
    if (plugin.enabled()) {
      badge.setText("Enabled");
      badge.getElement().getThemeList().add("badge success");
    } else {
      badge.setText("Disabled");
      badge.getElement().getThemeList().add("badge error");
    }
    return badge;
  }

  private Component createActionsColumn(PluginInfo plugin) {
    Button editButton = new Button(new Icon(VaadinIcon.COG));
    editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    editButton.setTooltipText("Configure Plugin");
    editButton.addClickListener(e -> openConfigDialog(plugin));

    Button toggleButton = new Button(new Icon(plugin.enabled() ? VaadinIcon.PAUSE : VaadinIcon.PLAY));
    toggleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
    toggleButton.setTooltipText(plugin.enabled() ? "Disable Plugin" : "Enable Plugin");
    toggleButton.addClickListener(e -> togglePlugin(plugin));

    HorizontalLayout actions = new HorizontalLayout(editButton, toggleButton);
    actions.setSpacing(false);
    return actions;
  }

  private Component createPluginDetails(PluginInfo plugin) {
    VerticalLayout details = new VerticalLayout();
    details.setPadding(true);
    details.addClassNames(LumoUtility.Background.CONTRAST_5);

    // Configuration section
    H4 configTitle = new H4("Current Configuration");

    if (plugin.configuration() != null && !plugin.configuration().isEmpty()) {
      VerticalLayout configLayout = new VerticalLayout();
      configLayout.setPadding(false);
      configLayout.setSpacing(false);

      plugin.configuration().forEach((key, value) -> {
        HorizontalLayout configItem = new HorizontalLayout();
        Span keySpan = new Span(key + ":");
        keySpan.addClassNames(LumoUtility.FontWeight.MEDIUM);
        Span valueSpan = new Span(String.valueOf(value));

        configItem.add(keySpan, valueSpan);
        configItem.addClassNames(LumoUtility.FontSize.SMALL);
        configLayout.add(configItem);
      });

      details.add(configTitle, configLayout);
    } else {
      details.add(configTitle, new Span("No configuration available"));
    }

    return details;
  }

  private void configureFilters() {
    phaseFilter.setLabel("Filter by Phase");
    phaseFilter.setItems("All", "PRE_AUTH", "AUTH", "POST_AUTH", "PRE_REQUEST", "POST_REQUEST", "ERROR");
    phaseFilter.setValue("All");
    phaseFilter.addValueChangeListener(e -> updateList());

    searchField.setPlaceholder("Search plugins...");
    searchField.setClearButtonVisible(true);
    searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
    searchField.addValueChangeListener(e -> updateList());
  }

  private HorizontalLayout getToolbar() {
    Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
    refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    refreshButton.addClickListener(click -> updateList());

    Button installButton = new Button("Install Plugin", new Icon(VaadinIcon.DOWNLOAD));
    installButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    installButton.addClickListener(click -> openInstallDialog());

    HorizontalLayout toolbar = new HorizontalLayout(phaseFilter, searchField, refreshButton, installButton);
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

  private void openConfigDialog(PluginInfo plugin) {
    PluginConfigForm form = new PluginConfigForm(plugin);
    Dialog dialog = new Dialog(form);

    dialog.setHeaderTitle("Configure " + plugin.name());
    dialog.setModal(true);
    dialog.setDraggable(true);
    dialog.setResizable(true);
    dialog.setWidth("700px");
    dialog.setHeight("500px");

    Button saveButton = new Button("Save Configuration", e -> {
      try {
        Map<String, Object> config = form.configuration();
        pluginService.updatePluginConfiguration(plugin.name(), config);
        updateList();
        dialog.close();
        showNotification("Plugin configuration updated", NotificationVariant.LUMO_SUCCESS);
      } catch (Exception ex) {
        showNotification("Error updating configuration: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
      }
    });
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button cancelButton = new Button("Cancel", e -> dialog.close());

    dialog.getFooter().add(cancelButton, saveButton);
    dialog.open();
  }

  private void openInstallDialog() {
    // Placeholder for plugin installation dialog
    showNotification("Plugin installation feature coming soon", NotificationVariant.LUMO_CONTRAST);
  }

  private void togglePlugin(PluginInfo plugin) {
    try {
      if (plugin.enabled()) {
        pluginService.disablePlugin(plugin.name());
        showNotification("Plugin disabled", NotificationVariant.LUMO_SUCCESS);
      } else {
        pluginService.enablePlugin(plugin.name());
        showNotification("Plugin enabled", NotificationVariant.LUMO_SUCCESS);
      }
      updateList();
    } catch (Exception e) {
      showNotification("Error toggling plugin: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
    }
  }

  private void updateList() {
    String searchTerm = searchField.getValue();
    String phaseFilter = this.phaseFilter.getValue();

    List<PluginInfo> plugins = pluginService.getAllPlugins();

    // Apply phase filter
    if (phaseFilter != null && !"All".equals(phaseFilter)) {
      plugins = plugins.stream()
          .filter(p -> phaseFilter.equals(p.phase().name()))
          .collect(Collectors.toList());
    }

    // Apply search filter
    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
      plugins = plugins.stream()
          .filter(p -> p.name().toLowerCase().contains(searchTerm.toLowerCase()) ||
              (p.description() != null && p.description().toLowerCase().contains(searchTerm.toLowerCase())))
          .collect(Collectors.toList());
    }

    grid.setItems(plugins);
  }

  private void showNotification(String message, NotificationVariant variant) {
    Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
    notification.addThemeVariants(variant);
  }

  // Plugin Configuration Form
  private static class PluginConfigForm extends VerticalLayout {
    private final PluginInfo plugin;
    private final VerticalLayout configFields;

    public PluginConfigForm(PluginInfo plugin) {
      this.plugin = plugin;
      this.configFields = new VerticalLayout();

      setSizeFull();
      setPadding(false);

      H3 title = new H3("Plugin Configuration");

      // Plugin info
      Span nameSpan = new Span("Name: " + plugin.name());
      nameSpan.addClassNames(LumoUtility.FontWeight.MEDIUM);

      Span phaseSpan = new Span("Phase: " + plugin.phase().name());
      phaseSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

      Span orderSpan = new Span("Order: " + plugin.order());
      orderSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

      HorizontalLayout infoLayout = new HorizontalLayout(nameSpan, phaseSpan, orderSpan);
      infoLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

      // Configuration fields
      configFields.setPadding(false);
      buildConfigurationFields();

      add(title, infoLayout, configFields);
    }

    private void buildConfigurationFields() {
      if (plugin.configuration() == null || plugin.configuration().isEmpty()) {
        configFields.add(new Span("No configuration options available"));
        return;
      }

      FormLayout formLayout = new FormLayout();

      plugin.configuration().forEach((key, value) -> {
        Component field = createConfigField(key, value);
        formLayout.add(field);
      });

      formLayout.setResponsiveSteps(
          new FormLayout.ResponsiveStep("0", 1),
          new FormLayout.ResponsiveStep("500px", 2)
      );

      configFields.add(formLayout);
    }

    private Component createConfigField(String key, Object value) {
      if (value instanceof Boolean) {
        Checkbox checkbox = new Checkbox(formatFieldLabel(key));
        checkbox.setValue((Boolean) value);
        checkbox.getElement().setAttribute("config-key", key);
        return checkbox;
      } else if (value instanceof Number) {
        IntegerField numberField = new IntegerField(formatFieldLabel(key));
        numberField.setValue(((Number) value).intValue());
        numberField.getElement().setAttribute("config-key", key);
        return numberField;
      } else {
        TextField textField = new TextField(formatFieldLabel(key));
        textField.setValue(String.valueOf(value));
        textField.getElement().setAttribute("config-key", key);
        return textField;
      }
    }

    private String formatFieldLabel(String key) {
      return key.substring(0, 1).toUpperCase() +
          key.substring(1).replaceAll("([A-Z])", " $1");
    }

    public Map<String, Object> configuration() {
      return configFields.getChildren()
          .filter(component -> component instanceof FormLayout)
          .flatMap(formLayout -> ((FormLayout) formLayout).getChildren())
          .collect(Collectors.toMap(
              component -> component.getElement().getAttribute("config-key"),
              this::getFieldValue
          ));
    }

    private Object getFieldValue(Component component) {
      if (component instanceof Checkbox) {
        return ((Checkbox) component).getValue();
      } else if (component instanceof IntegerField) {
        return ((IntegerField) component).getValue();
      } else if (component instanceof TextField) {
        return ((TextField) component).getValue();
      }
      return null;
    }
  }
}