package io.github.dumijdev.belanova.gateway.admin.ui.layouts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import io.github.dumijdev.belanova.gateway.admin.ui.services.ThemeService;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;
import io.github.dumijdev.belanova.gateway.admin.ui.views.*;

@Layout
@CssImport("./styles/main-layout.css")
public class MainLayout extends AppLayout {

  private final ThemeService themeService;
  private final TranslationService translationService;
  private Button themeToggle;
  private Select<String> languageSelect;

  public MainLayout(ThemeService themeService, TranslationService translationService) {
    this.themeService = themeService;
    this.translationService = translationService;

    setPrimarySection(Section.DRAWER);
    addClassName("main-layout");

    // Apply current theme
    getElement().setAttribute("theme", themeService.getCurrentTheme());

    addToNavbar(createHeader());
    addToDrawer(createDrawer());
  }

  private HorizontalLayout createHeader() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.addClassName("drawer-toggle");
    toggle.setAriaLabel(translationService.getTranslation("menu.toggle"));

    // Logo and brand
    HorizontalLayout logoSection = createLogoSection();

    // Controls section
    HorizontalLayout controls = createControlsSection();

    HorizontalLayout header = new HorizontalLayout(toggle, logoSection, controls);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.setWidthFull();
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.addClassName("app-header");

    return header;
  }

  private HorizontalLayout createLogoSection() {
    Icon logoIcon = VaadinIcon.GLOBE_WIRE.create();
    logoIcon.addClassName("logo-icon");

    Span brandName = new Span(translationService.getTranslation("app.name"));
    brandName.addClassName("brand-name");

    Span version = new Span("v1.0");
    version.addClassName("version-badge");

    HorizontalLayout logoSection = new HorizontalLayout(logoIcon, brandName, version);
    logoSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    logoSection.setSpacing(false);
    logoSection.addClassName("logo-section");

    return logoSection;
  }

  private HorizontalLayout createControlsSection() {
    // Language selector
    languageSelect = new Select<>();
    languageSelect.setItems("en", "pt", "es", "fr");
    languageSelect.setValue(translationService.getCurrentLanguage());
    languageSelect.setRenderer(new ComponentRenderer<>(this::createLanguageOption));
    languageSelect.addValueChangeListener(e -> {
      translationService.setLanguage(e.getValue());
      getUI().ifPresent(ui -> ui.getPage().reload());
    });
    languageSelect.addClassName("language-select");

    // Theme toggle
    themeToggle = new Button();
    updateThemeToggle();
    themeToggle.addClickListener(e -> toggleTheme());
    themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
    themeToggle.addClassName("theme-toggle");

    // User menu placeholder
    Button userMenu = new Button(VaadinIcon.USER.create());
    userMenu.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
    userMenu.addClassName("user-menu");
    userMenu.setAriaLabel(translationService.getTranslation("menu.user"));

    HorizontalLayout controls = new HorizontalLayout(languageSelect, themeToggle, userMenu);
    controls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    controls.setSpacing(true);
    controls.addClassName("header-controls");

    return controls;
  }

  private Component createLanguageOption(String language) {
    Icon flag = switch (language) {
      case "en" -> VaadinIcon.FLAG.create();
      case "pt" -> VaadinIcon.FLAG.create();
      case "es" -> VaadinIcon.FLAG.create();
      case "fr" -> VaadinIcon.FLAG.create();
      default -> VaadinIcon.GLOBE.create();
    };

    String languageName = translationService.getTranslation("language." + language);

    HorizontalLayout option = new HorizontalLayout(flag, new Span(languageName));
    option.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    option.setSpacing(false);

    return option;
  }

  private VerticalLayout createDrawer() {
    VerticalLayout drawer = new VerticalLayout();
    drawer.addClassName("app-drawer");
    drawer.setSpacing(false);
    drawer.setPadding(false);

    // Navigation header
    Div navHeader = new Div();
    navHeader.addClassName("nav-header");
    Span navTitle = new Span(translationService.getTranslation("navigation.title"));
    navTitle.addClassName("nav-title");
    navHeader.add(navTitle);

    // Navigation items
    VerticalLayout navItems = new VerticalLayout();
    navItems.addClassName("nav-items");
    navItems.setSpacing(false);
    navItems.setPadding(false);

    navItems.add(
        createNavItem("navigation.dashboard", VaadinIcon.DASHBOARD, DashboardView.class),
        createNavItem("navigation.backends", VaadinIcon.SERVER, BackendsView.class),
        createNavItem("navigation.services", VaadinIcon.GLOBE, ServicesView.class),
        createNavItem("navigation.upstreams", VaadinIcon.SHARE_SQUARE, UpstreamsView.class),
        createNavItem("navigation.routes", VaadinIcon.ROAD, RoutesView.class),
        createNavSeparator(),
        createNavItem("navigation.health", VaadinIcon.HEART, HealthChecksView.class),
        createNavItem("navigation.plugins", VaadinIcon.PLUG, PluginsView.class),
        createNavItem("navigation.monitoring", VaadinIcon.LINE_CHART, MonitoringView.class),
        createNavSeparator(),
        createNavItem("navigation.settings", VaadinIcon.COG, SettingsView.class)
    );

    drawer.add(navHeader, navItems);
    return drawer;
  }

  private RouterLink createNavItem(String textKey, VaadinIcon vaadinIcon, Class<? extends Component> component) {
    Icon icon = vaadinIcon.create();
    icon.addClassName("nav-icon");

    Span text = new Span(translationService.getTranslation(textKey));
    text.addClassName("nav-text");

    RouterLink link = new RouterLink();
    link.add(icon, text);
    link.setRoute(component);
    link.addClassName("nav-item");
    link.getElement().setAttribute("aria-label", translationService.getTranslation(textKey));

    return link;
  }

  private Div createNavSeparator() {
    Div separator = new Div();
    separator.addClassName("nav-separator");
    return separator;
  }

  private void toggleTheme() {
    boolean isDark = themeService.isDarkMode();
    String newTheme = isDark ? Lumo.LIGHT : Lumo.DARK;

    themeService.setTheme(newTheme);
    getElement().setAttribute("theme", newTheme);
    updateThemeToggle();
  }

  private void updateThemeToggle() {
    boolean isDark = themeService.isDarkMode();
    themeToggle.setIcon(isDark ? VaadinIcon.SUN_O.create() : VaadinIcon.MOON_O.create());
    themeToggle.setAriaLabel(translationService.getTranslation(
        isDark ? "theme.light" : "theme.dark"));
  }
}