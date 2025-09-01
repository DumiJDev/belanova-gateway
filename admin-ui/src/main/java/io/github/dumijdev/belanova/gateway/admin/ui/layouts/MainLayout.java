package io.github.dumijdev.belanova.gateway.admin.ui.layouts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.dumijdev.belanova.gateway.admin.ui.services.ThemeService;
import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;
import io.github.dumijdev.belanova.gateway.admin.ui.views.*;
import org.springframework.beans.factory.annotation.Autowired;

public class MainLayout extends AppLayout {

  private H2 viewTitle;
  private final ThemeService themeService;
  private final TranslationService translationService;

  @Autowired
  public MainLayout(ThemeService themeService, TranslationService translationService) {
    this.themeService = themeService;
    this.translationService = translationService;

    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
    applyCurrentTheme();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel(translationService.getTranslation("menu.toggle"));

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    // Theme toggle button
    Button themeToggle = new Button(new Icon(themeService.isDarkMode() ? VaadinIcon.SUN_O : VaadinIcon.MOON_O));
    themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    themeToggle.setTooltipText(themeService.isDarkMode() ?
        translationService.getTranslation("theme.light") :
        translationService.getTranslation("theme.dark"));
    themeToggle.addClickListener(e -> toggleTheme());

    // Language selector button
    Button languageButton = new Button(translationService.getLanguageDisplayName(translationService.getCurrentLanguage()));
    languageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    languageButton.setTooltipText("Change Language");

    // Language context menu
    ContextMenu languageMenu = new ContextMenu(languageButton);
    languageMenu.setOpenOnClick(true);

    for (String lang : translationService.getSupportedLanguages()) {
      languageMenu.addItem(translationService.getLanguageDisplayName(lang), e -> {
        translationService.setLanguage(lang);
        languageButton.setText(translationService.getLanguageDisplayName(lang));
        refreshUI();
      });
    }

    HorizontalLayout headerLeft = new HorizontalLayout(toggle, viewTitle);
    headerLeft.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    headerLeft.expand(viewTitle);

    HorizontalLayout headerRight = new HorizontalLayout(themeToggle, languageButton);
    headerRight.setSpacing(true);

    HorizontalLayout header = new HorizontalLayout(headerLeft, headerRight);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.expand(headerLeft);
    header.setWidthFull();
    header.addClassNames(
        LumoUtility.Padding.Vertical.NONE,
        LumoUtility.Padding.Horizontal.MEDIUM
    );

    addToNavbar(header);
  }

  private void applyCurrentTheme() {
    String theme = themeService.getCurrentTheme();
    UI.getCurrent().getElement().setAttribute("theme", theme);
  }

  private void toggleTheme() {
    themeService.toggleTheme();
    applyCurrentTheme();

    // Update theme toggle icon
    getChildren().filter(component -> component instanceof HorizontalLayout)
        .findFirst()
        .ifPresent(header -> {
          header.getChildren().filter(component -> component instanceof HorizontalLayout)
              .findFirst()
              .ifPresent(headerRight -> {
                headerRight.getChildren().filter(component -> component instanceof Button)
                    .findFirst()
                    .ifPresent(button -> {
                      if (button instanceof Button) {
                        Button themeButton = (Button) button;
                        Icon icon = (Icon) themeButton.getIcon();
                        if (icon != null) {
                          icon.setIcon(themeService.isDarkMode() ? VaadinIcon.SUN_O : VaadinIcon.MOON_O);
                          themeButton.setTooltipText(themeService.isDarkMode() ?
                              translationService.getTranslation("theme.light") :
                              translationService.getTranslation("theme.dark"));
                        }
                      }
                    });
              });
        });
  }

  private void refreshUI() {
    // Refresh the current view to apply new translations
    UI.getCurrent().getPage().reload();
  }

  private void addDrawerContent() {
    H1 appName = new H1(translationService.getTranslation("app.name"));
    appName.addClassNames(
        LumoUtility.FontSize.LARGE,
        LumoUtility.Margin.NONE,
        LumoUtility.TextColor.PRIMARY_CONTRAST
    );

    Span version = new Span(translationService.getTranslation("app.version"));
    version.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

    VerticalLayout brandLayout = new VerticalLayout(appName, version);
    brandLayout.addClassNames(LumoUtility.Padding.MEDIUM);
    brandLayout.setSpacing(false);

    Header header = new Header(brandLayout);
    header.addClassName("kong-header");

    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();
    nav.addClassName("kong-nav");

    nav.addItem(new SideNavItem(translationService.getTranslation("nav.dashboard"), DashboardView.class, VaadinIcon.DASHBOARD.create()));
    nav.addItem(new SideNavItem(translationService.getTranslation("nav.backends"), BackendsView.class, VaadinIcon.SERVER.create()));
    nav.addItem(new SideNavItem(translationService.getTranslation("nav.services"), ServicesView.class, VaadinIcon.COGS.create()));
    nav.addItem(new SideNavItem(translationService.getTranslation("nav.routes"), RoutesView.class, VaadinIcon.ROAD.create()));
    nav.addItem(new SideNavItem(translationService.getTranslation("nav.consumers"), ConsumersView.class, VaadinIcon.USERS.create()));
    nav.addItem(new SideNavItem(translationService.getTranslation("nav.analytics"), AnalyticsView.class, VaadinIcon.CHART.create()));
    nav.addItem(new SideNavItem(translationService.getTranslation("nav.alerts"), AlertsView.class, VaadinIcon.BELL.create()));
    nav.addItem(new SideNavItem(translationService.getTranslation("nav.plugins"), PluginsView.class, VaadinIcon.PLUG.create()));

    // Monitoring section
    SideNavItem monitoring = new SideNavItem(translationService.getTranslation("nav.monitoring"), MonitoringView.class, VaadinIcon.CHART.create());
    nav.addItem(monitoring);

    // Settings section
    SideNavItem settings = new SideNavItem(translationService.getTranslation("nav.settings"), SettingsView.class, VaadinIcon.COG.create());
    nav.addItem(settings);

    return nav;
  }

  @Override
  protected void afterNavigation() {
    super.afterNavigation();
    viewTitle.setText(getCurrentPageTitle());
  }

  private String getCurrentPageTitle() {
    PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
    return title == null ? "" : title.value();
  }
}