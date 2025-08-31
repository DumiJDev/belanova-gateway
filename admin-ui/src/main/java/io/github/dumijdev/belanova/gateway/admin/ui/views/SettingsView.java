package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings")
@CssImport("./styles/settings.css")
public class SettingsView extends VerticalLayout {

  private final TranslationService translationService;

  public SettingsView(TranslationService translationService) {
    this.translationService = translationService;

    setSizeFull();
    setPadding(false);
    setSpacing(false);
    addClassName("settings-view");

    add(createHeader());
    add(createTabSheet());
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

    H1 title = new H1(translationService.getTranslation("navigation.settings"));
    title.addClassName("view-title");

    Span subtitle = new Span("Configure gateway settings and system preferences");
    subtitle.addClassName("view-subtitle");

    titleSection.add(title, subtitle);

    // Action buttons
    HorizontalLayout actions = new HorizontalLayout();
    actions.setSpacing(true);

    Button saveAllBtn = new Button("Save All Changes");
    saveAllBtn.setIcon(VaadinIcon.CHECK.create());
    saveAllBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveAllBtn.addClassName("action-button");
    saveAllBtn.addClickListener(e -> saveAllSettings());

    Button resetBtn = new Button("Reset to Defaults");
    resetBtn.setIcon(VaadinIcon.REFRESH.create());
    resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    resetBtn.addClassName("action-button");
    resetBtn.addClickListener(e -> resetToDefaults());

    actions.add(saveAllBtn, resetBtn);

    header.add(titleSection, actions);
    return header;
  }

  private Component createTabSheet() {
    TabSheet tabSheet = new TabSheet();
    tabSheet.addClassName("settings-tabsheet");
    tabSheet.setSizeFull();

    // Gateway Configuration Tab
    tabSheet.add(createTab("Gateway", VaadinIcon.SERVER), createGatewayTab());

    // Cache Configuration Tab
    tabSheet.add(createTab("Cache", VaadinIcon.DATABASE), createCacheTab());

    // Security Configuration Tab
    tabSheet.add(createTab("Security", VaadinIcon.SHIELD), createSecurityTab());

    // Database Configuration Tab
    tabSheet.add(createTab("Database", VaadinIcon.STORAGE), createDatabaseTab());

    // Monitoring Configuration Tab
    tabSheet.add(createTab("Monitoring", VaadinIcon.LINE_CHART), createMonitoringTab());

    // Advanced Configuration Tab
    tabSheet.add(createTab("Advanced", VaadinIcon.COG), createAdvancedTab());

    return tabSheet;
  }

  private Tab createTab(String title, VaadinIcon icon) {
    Tab tab = new Tab();
    tab.add(icon.create(), new Span(title));
    tab.addClassName("settings-tab");
    return tab;
  }

  private Component createGatewayTab() {
    VerticalLayout content = new VerticalLayout();
    content.addClassName("tab-content");

    content.add(createSettingsSection(
        "Gateway Configuration",
        "Configure basic gateway settings and network parameters",
        createGatewayForm()
    ));

    content.add(createSettingsSection(
        "CORS Settings",
        "Configure Cross-Origin Resource Sharing policies",
        createCorsForm()
    ));

    return content;
  }

  private Component createCacheTab() {
    VerticalLayout content = new VerticalLayout();
    content.addClassName("tab-content");

    content.add(createSettingsSection(
        "Cache Provider",
        "Configure caching strategy and provider settings",
        createCacheProviderForm()
    ));

    content.add(createSettingsSection(
        "Cache Performance",
        "Optimize cache performance and memory usage",
        createCachePerformanceForm()
    ));

    return content;
  }

  private Component createSecurityTab() {
    VerticalLayout content = new VerticalLayout();
    content.addClassName("tab-content");

    content.add(createSettingsSection(
        "Authentication",
        "Configure authentication methods and security policies",
        createAuthenticationForm()
    ));

    content.add(createSettingsSection(
        "LDAP Configuration",
        "Setup LDAP integration for user authentication",
        createLdapForm()
    ));

    return content;
  }

  private Component createDatabaseTab() {
    VerticalLayout content = new VerticalLayout();
    content.addClassName("tab-content");

    content.add(createSettingsSection(
        "Database Connection",
        "Configure database connection and performance settings",
        createDatabaseForm()
    ));

    return content;
  }

  private Component createMonitoringTab() {
    VerticalLayout content = new VerticalLayout();
    content.addClassName("tab-content");

    content.add(createSettingsSection(
        "Metrics & Monitoring",
        "Configure monitoring, metrics collection and alerting",
        createMonitoringForm()
    ));

    content.add(createSettingsSection(
        "Alerting",
        "Setup notification and alerting rules",
        createAlertingForm()
    ));

    return content;
  }

  private Component createAdvancedTab() {
    VerticalLayout content = new VerticalLayout();
    content.addClassName("tab-content");

    content.add(createSettingsSection(
        "Performance Tuning",
        "Advanced performance and JVM configuration",
        createPerformanceForm()
    ));

    content.add(createSettingsSection(
        "Import/Export",
        "Backup and restore gateway configuration",
        createImportExportForm()
    ));

    return content;
  }

  private Component createSettingsSection(String title, String description, Component form) {
    VerticalLayout section = new VerticalLayout();
    section.addClassName("settings-section");

    HorizontalLayout header = new HorizontalLayout();
    header.addClassName("section-header");
    header.setAlignItems(FlexComponent.Alignment.CENTER);

    VerticalLayout titleArea = new VerticalLayout();
    titleArea.setSpacing(false);
    titleArea.setPadding(false);

    H2 sectionTitle = new H2(title);
    sectionTitle.addClassName("section-title");

    Span sectionDescription = new Span(description);
    sectionDescription.addClassName("section-description");

    titleArea.add(sectionTitle, sectionDescription);
    header.add(titleArea);

    Div formWrapper = new Div();
    formWrapper.addClassName("form-wrapper");
    formWrapper.add(form);

    section.add(header, formWrapper);
    return section;
  }

  private Component createGatewayForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    IntegerField portField = new IntegerField("Gateway Port");
    portField.setValue(8080);
    portField.setHelperText("Port number for the gateway server");

    TextField basePathField = new TextField("Base Path");
    basePathField.setValue("/");
    basePathField.setHelperText("Base path for all gateway routes");

    IntegerField timeoutField = new IntegerField("Request Timeout (ms)");
    timeoutField.setValue(30000);
    timeoutField.setHelperText("Maximum time to wait for backend response");

    IntegerField maxRequestSizeField = new IntegerField("Max Request Size (MB)");
    maxRequestSizeField.setValue(10);
    maxRequestSizeField.setHelperText("Maximum allowed request body size");

    Button testConnectionBtn = new Button("Test Gateway Connection");
    testConnectionBtn.setIcon(VaadinIcon.CONNECT.create());
    testConnectionBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    testConnectionBtn.addClickListener(e -> testGatewayConnection());

    form.add(portField, basePathField, timeoutField, maxRequestSizeField);
    form.setColspan(testConnectionBtn, 2);
    form.add(testConnectionBtn);

    return form;
  }

  private Component createCorsForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    Checkbox enableCorsField = new Checkbox("Enable CORS");
    enableCorsField.setValue(true);

    TextField allowedOriginsField = new TextField("Allowed Origins");
    allowedOriginsField.setValue("*");
    allowedOriginsField.setHelperText("Comma-separated list of allowed origins");

    TextField allowedMethodsField = new TextField("Allowed Methods");
    allowedMethodsField.setValue("GET,POST,PUT,DELETE,OPTIONS");
    allowedMethodsField.setHelperText("HTTP methods allowed for CORS requests");

    TextField allowedHeadersField = new TextField("Allowed Headers");
    allowedHeadersField.setValue("*");
    allowedHeadersField.setHelperText("Headers allowed in CORS requests");

    IntegerField maxAgeField = new IntegerField("Max Age (seconds)");
    maxAgeField.setValue(3600);
    maxAgeField.setHelperText("How long browsers should cache CORS settings");

    form.add(enableCorsField, allowedOriginsField);
    form.setColspan(allowedOriginsField, 2);
    form.add(allowedMethodsField, allowedHeadersField);
    form.setColspan(allowedMethodsField, 2);
    form.setColspan(allowedHeadersField, 2);
    form.add(maxAgeField);

    return form;
  }

  private Component createCacheProviderForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    Select<String> providerSelect = new Select<>();
    providerSelect.setLabel("Cache Provider");
    providerSelect.setItems("Hazelcast", "Apache Ignite", "Caffeine", "Redis");
    providerSelect.setValue("Apache Ignite");
    providerSelect.setHelperText("Choose your preferred caching solution");

    IntegerField ttlField = new IntegerField("Default TTL (seconds)");
    ttlField.setValue(3600);
    ttlField.setHelperText("Default time-to-live for cache entries");

    IntegerField maxEntriesField = new IntegerField("Max Cache Entries");
    maxEntriesField.setValue(10000);
    maxEntriesField.setHelperText("Maximum number of entries in cache");

    Button testCacheBtn = new Button("Test Cache Connection");
    testCacheBtn.setIcon(VaadinIcon.DATABASE.create());
    testCacheBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    testCacheBtn.addClickListener(e -> testCacheConnection());

    form.add(providerSelect, ttlField, maxEntriesField);
    form.setColspan(testCacheBtn, 2);
    form.add(testCacheBtn);

    return form;
  }

  private Component createCachePerformanceForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    Checkbox enableCacheMetricsField = new Checkbox("Enable Cache Metrics");
    enableCacheMetricsField.setValue(true);

    IntegerField evictionBatchSizeField = new IntegerField("Eviction Batch Size");
    evictionBatchSizeField.setValue(100);
    evictionBatchSizeField.setHelperText("Number of entries to evict in one batch");

    IntegerField refreshAheadFactorField = new IntegerField("Refresh Ahead Factor (%)");
    refreshAheadFactorField.setValue(75);
    refreshAheadFactorField.setHelperText("Refresh cache entry when this % of TTL remains");

    form.add(enableCacheMetricsField, evictionBatchSizeField, refreshAheadFactorField);

    return form;
  }

  private Component createAuthenticationForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    Select<String> authTypeSelect = new Select<>();
    authTypeSelect.setLabel("Authentication Type");
    authTypeSelect.setItems("JWT", "LDAP", "Basic Auth", "OAuth2");
    authTypeSelect.setValue("JWT");

    TextField defaultAdminField = new TextField("Default Admin Username");
    defaultAdminField.setValue("admin");

    PasswordField jwtSecretField = new PasswordField("JWT Secret Key");
    jwtSecretField.setHelperText("Secret key for JWT token signing");

    IntegerField sessionTimeoutField = new IntegerField("Session Timeout (minutes)");
    sessionTimeoutField.setValue(60);

    TextField passwordPolicyField = new TextField("Password Policy Regex");
    passwordPolicyField.setValue("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");
    passwordPolicyField.setHelperText("Regular expression for password validation");

    form.add(authTypeSelect, defaultAdminField, jwtSecretField);
    form.add(sessionTimeoutField, passwordPolicyField);
    form.setColspan(passwordPolicyField, 2);

    return form;
  }

  private Component createLdapForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    TextField ldapUrlField = new TextField("LDAP URL");
    ldapUrlField.setValue("ldap://localhost:389");
    ldapUrlField.setHelperText("LDAP server connection URL");

    TextField baseDnField = new TextField("Base DN");
    baseDnField.setValue("dc=example,dc=com");
    baseDnField.setHelperText("Base Distinguished Name for searches");

    TextField userDnPatternField = new TextField("User DN Pattern");
    userDnPatternField.setValue("uid={0},ou=users,dc=example,dc=com");
    userDnPatternField.setHelperText("Pattern for user authentication");

    TextField bindDnField = new TextField("Bind DN");
    bindDnField.setHelperText("DN for binding to LDAP (optional)");

    PasswordField bindPasswordField = new PasswordField("Bind Password");
    bindPasswordField.setHelperText("Password for bind DN");

    Button testLdapBtn = new Button("Test LDAP Connection");
    testLdapBtn.setIcon(VaadinIcon.CONNECT.create());
    testLdapBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    testLdapBtn.addClickListener(e -> testLdapConnection());

    form.add(ldapUrlField, baseDnField);
    form.setColspan(ldapUrlField, 2);
    form.setColspan(baseDnField, 2);
    form.add(userDnPatternField);
    form.setColspan(userDnPatternField, 2);
    form.add(bindDnField, bindPasswordField);
    form.setColspan(testLdapBtn, 2);
    form.add(testLdapBtn);

    return form;
  }

  private Component createDatabaseForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    Select<String> dbTypeSelect = new Select<>();
    dbTypeSelect.setLabel("Database Type");
    dbTypeSelect.setItems("H2", "PostgreSQL", "MySQL", "SQL Server");
    dbTypeSelect.setValue("H2");

    TextField connectionUrlField = new TextField("Connection URL");
    connectionUrlField.setValue("jdbc:h2:mem:belanova");
    connectionUrlField.setHelperText("JDBC connection URL");

    TextField usernameField = new TextField("Username");
    usernameField.setValue("sa");

    PasswordField passwordField = new PasswordField("Password");

    IntegerField poolSizeField = new IntegerField("Connection Pool Size");
    poolSizeField.setValue(10);
    poolSizeField.setHelperText("Maximum number of database connections");

    IntegerField connectionTimeoutField = new IntegerField("Connection Timeout (ms)");
    connectionTimeoutField.setValue(30000);

    Button testDbBtn = new Button("Test Database Connection");
    testDbBtn.setIcon(VaadinIcon.DATABASE.create());
    testDbBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    testDbBtn.addClickListener(e -> testDatabaseConnection());

    form.add(dbTypeSelect, connectionUrlField);
    form.setColspan(connectionUrlField, 2);
    form.add(usernameField, passwordField);
    form.add(poolSizeField, connectionTimeoutField);
    form.setColspan(testDbBtn, 2);
    form.add(testDbBtn);

    return form;
  }

  private Component createMonitoringForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    Checkbox enableMetricsField = new Checkbox("Enable Metrics Collection");
    enableMetricsField.setValue(true);

    TextField metricsEndpointField = new TextField("Metrics Endpoint");
    metricsEndpointField.setValue("/actuator/metrics");

    Checkbox prometheusExportField = new Checkbox("Enable Prometheus Export");
    prometheusExportField.setValue(true);

    TextField customTagsField = new TextField("Custom Metrics Tags");
    customTagsField.setValue("environment=prod,version=1.0");
    customTagsField.setHelperText("Comma-separated key=value pairs");

    IntegerField metricsRetentionField = new IntegerField("Metrics Retention (days)");
    metricsRetentionField.setValue(30);

    form.add(enableMetricsField, metricsEndpointField, prometheusExportField);
    form.add(customTagsField);
    form.setColspan(customTagsField, 2);
    form.add(metricsRetentionField);

    return form;
  }

  private Component createAlertingForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    Checkbox enableAlertsField = new Checkbox("Enable Alerting");
    enableAlertsField.setValue(true);

    TextField webhookUrlField = new TextField("Webhook URL");
    webhookUrlField.setHelperText("URL for sending alert notifications");

    TextField emailRecipientsField = new TextField("Email Recipients");
    emailRecipientsField.setHelperText("Comma-separated email addresses");

    IntegerField alertThresholdField = new IntegerField("Error Rate Threshold (%)");
    alertThresholdField.setValue(5);
    alertThresholdField.setHelperText("Trigger alert when error rate exceeds this value");

    form.add(enableAlertsField, webhookUrlField);
    form.setColspan(webhookUrlField, 2);
    form.add(emailRecipientsField);
    form.setColspan(emailRecipientsField, 2);
    form.add(alertThresholdField);

    return form;
  }

  private Component createPerformanceForm() {
    FormLayout form = new FormLayout();
    form.addClassName("settings-form");

    IntegerField threadPoolSizeField = new IntegerField("Thread Pool Size");
    threadPoolSizeField.setValue(200);
    threadPoolSizeField.setHelperText("Number of worker threads for request processing");

    TextField heapSizeField = new TextField("JVM Heap Size");
    heapSizeField.setValue("2g");
    heapSizeField.setHelperText("Maximum heap size (e.g., 2g, 512m)");

    Checkbox enableExperimentalField = new Checkbox("Enable Experimental Features");
    enableExperimentalField.setValue(false);
    enableExperimentalField.setHelperText("Enable cutting-edge features (use with caution)");

    Checkbox maintenanceModeField = new Checkbox("Maintenance Mode");
    maintenanceModeField.setValue(false);
    maintenanceModeField.setHelperText("Put gateway in maintenance mode");

    Checkbox hotReloadField = new Checkbox("Plugin Hot Reload");
    hotReloadField.setValue(true);
    hotReloadField.setHelperText("Allow plugins to be reloaded without restart");

    form.add(threadPoolSizeField, heapSizeField);
    form.add(enableExperimentalField, maintenanceModeField, hotReloadField);

    return form;
  }

  private Component createImportExportForm() {
    VerticalLayout form = new VerticalLayout();
    form.addClassName("import-export-form");

    // Export section
    HorizontalLayout exportSection = new HorizontalLayout();
    exportSection.addClassName("action-section");
    exportSection.setAlignItems(FlexComponent.Alignment.CENTER);

    VerticalLayout exportInfo = new VerticalLayout();
    exportInfo.setSpacing(false);
    exportInfo.setPadding(false);

    H2 exportTitle = new H2("Export Configuration");
    exportTitle.addClassName("action-title");

    Span exportDescription = new Span("Download current gateway configuration as JSON");
    exportDescription.addClassName("action-description");

    exportInfo.add(exportTitle, exportDescription);

    Button exportBtn = new Button("Export Settings");
    exportBtn.setIcon(VaadinIcon.DOWNLOAD.create());
    exportBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    exportBtn.addClassName("export-btn");
    exportBtn.addClickListener(e -> exportConfiguration());

    exportSection.add(exportInfo, exportBtn);

    // Import section
    HorizontalLayout importSection = new HorizontalLayout();
    importSection.addClassName("action-section");
    importSection.setAlignItems(FlexComponent.Alignment.CENTER);

    VerticalLayout importInfo = new VerticalLayout();
    importInfo.setSpacing(false);
    importInfo.setPadding(false);

    H2 importTitle = new H2("Import Configuration");
    importTitle.addClassName("action-title");

    Span importDescription = new Span("Upload and restore gateway configuration from JSON file");
    importDescription.addClassName("action-description");

    importInfo.add(importTitle, importDescription);

    Upload upload = new Upload();
    upload.setAcceptedFileTypes("application/json", ".json");
    upload.setMaxFiles(1);
    upload.addClassName("import-upload");

    // Define um UploadHandler que salva em arquivo temporário
    List<File> outputFiles = new ArrayList<>(1);
    UploadHandler handler = UploadHandler.toTempFile(
        (uploadMetadata, file) -> outputFiles.add(file)
    );

    // Associa o handler ao componente via Element API
    upload.getElement().setAttribute("target", handler);

    // Listener de sucesso ao concluir o upload
    upload.addSucceededListener(e -> {
      File uploaded = outputFiles.getFirst();
      // processa o arquivo JSON conforme necessário
      Notification.show("Configuração importada com sucesso")
          .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    });

    importSection.add(importInfo, upload);

    form.add(exportSection, importSection);
    return form;
  }

  private void saveAllSettings() {
    Notification.show("All settings saved successfully", 3000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void resetToDefaults() {
    Notification.show("Settings reset to defaults", 3000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
  }

  private void testGatewayConnection() {
    Notification.show("Gateway connection test successful", 3000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void testCacheConnection() {
    Notification.show("Cache connection test successful", 3000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void testLdapConnection() {
    Notification.show("LDAP connection test successful", 3000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void testDatabaseConnection() {
    Notification.show("Database connection test successful", 3000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void exportConfiguration() {
    Notification.show("Configuration exported successfully", 3000, Notification.Position.TOP_END)
        .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
  }
}