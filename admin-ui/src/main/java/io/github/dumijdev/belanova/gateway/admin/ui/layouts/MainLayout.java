package io.github.dumijdev.belanova.gateway.admin.ui.layouts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
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
import io.github.dumijdev.belanova.gateway.admin.ui.views.*;

public class MainLayout extends AppLayout {

  private H2 viewTitle;

  public MainLayout() {
    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel("Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    HorizontalLayout header = new HorizontalLayout(toggle, viewTitle);
    header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    header.expand(viewTitle);
    header.setWidthFull();
    header.addClassNames(
        LumoUtility.Padding.Vertical.NONE,
        LumoUtility.Padding.Horizontal.MEDIUM
    );

    addToNavbar(header);
  }

  private void addDrawerContent() {
    H1 appName = new H1("Belanova Gateway");
    appName.addClassNames(
        LumoUtility.FontSize.LARGE,
        LumoUtility.Margin.NONE,
        LumoUtility.TextColor.PRIMARY
    );

    Span version = new Span("v1.0.0");
    version.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

    VerticalLayout brandLayout = new VerticalLayout(appName, version);
    brandLayout.addClassNames(LumoUtility.Padding.MEDIUM);
    brandLayout.setSpacing(false);

    Header header = new Header(brandLayout);

    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();

    nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
    nav.addItem(new SideNavItem("Backends", BackendsView.class, VaadinIcon.SERVER.create()));
    nav.addItem(new SideNavItem("Services", ServicesView.class, VaadinIcon.COGS.create()));
    nav.addItem(new SideNavItem("Routes", RoutesView.class, VaadinIcon.ROAD.create()));
    nav.addItem(new SideNavItem("Plugins", PluginsView.class, VaadinIcon.PLUG.create()));

    // Monitoring section
    SideNavItem monitoring = new SideNavItem("Monitoring", "", VaadinIcon.CHART.create());
    monitoring.addItem(new SideNavItem("Health Checks", "", VaadinIcon.HEART.create()));
    monitoring.addItem(new SideNavItem("Metrics", "", VaadinIcon.BAR_CHART.create()));
    monitoring.addItem(new SideNavItem("Logs", "", VaadinIcon.FILE_TEXT.create()));
    nav.addItem(monitoring);

    // Settings section
    SideNavItem settings = new SideNavItem("Settings", SettingsView.class, VaadinIcon.COG.create());
    settings.addItem(new SideNavItem("Configuration", "", VaadinIcon.TOOLS.create()));
    settings.addItem(new SideNavItem("Cache", "", VaadinIcon.PACKAGE.create()));
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