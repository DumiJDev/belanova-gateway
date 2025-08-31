package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendRoute;
import io.github.dumijdev.belanova.gateway.admin.ui.services.RouteService;


@Route(value = "routes", layout = MainLayout.class)
@PageTitle("Routes • Belanova Gateway")
@CssImport("./styles/routes.css")
public class RoutesView extends VerticalLayout {

  private final RouteService routeService;
  private final Grid<BackendRoute> grid = new Grid<>(BackendRoute.class);

  public RoutesView(RouteService routeService) {
    this.routeService = routeService;
    addClassName("routes-view");
    setSizeFull();
    setPadding(false);
    add(createHeader(), createGrid());
    loadData();
  }

  private HorizontalLayout createHeader() {
    Span title = new Span("Routes");
    title.addClassName("view-title");

    Button addBtn = new Button("Add Route", VaadinIcon.PLUS.create());
    addBtn.addClassName("primary-action");
    addBtn.addClickListener(e -> openEditor(new BackendRoute(), "Add Route"));

    HorizontalLayout header = new HorizontalLayout(title, addBtn);
    header.addClassName("view-header");
    header.setWidthFull();
    header.setAlignItems(Alignment.CENTER);
    header.getStyle().set("justify-content", "space-between");
    return header;
  }

  private Grid<BackendRoute> createGrid() {
    grid.addClassName("routes-grid");
    grid.removeAllColumns();
    grid.addColumn(BackendRoute::status).setHeader("Status");
    grid.addColumn(BackendRoute::name).setHeader("Route Name").setSortable(true);
    grid.addColumn(BackendRoute::path).setHeader("Path");
    grid.addColumn(r -> String.join(", ", r.upstreamNames())).setHeader("Upstreams");
    grid.addColumn(r -> String.join(", ", r.methods())).setHeader("Methods");
    grid.addComponentColumn(r -> {
      Button edit = new Button(VaadinIcon.EDIT.create(), e -> openEditor(r, "Edit Route"));
      edit.addClassName("action-btn edit-btn");
      Button toggle = new Button(VaadinIcon.PLAY.create(), e -> { routeService.toggle(r); loadData(); });
      toggle.addClassName("action-btn toggle-btn");
      Button delete = new Button(VaadinIcon.TRASH.create(), e -> { routeService.delete(r); loadData(); });
      delete.addClassName("action-btn delete-btn");
      HorizontalLayout actions = new HorizontalLayout(edit, toggle, delete);
      actions.addClassName("actions-column");
      return actions;
    }).setHeader("Actions");

    grid.setWidthFull();
    grid.setHeight("60vh");
    return grid;
  }

  private void loadData() {
    grid.setItems(routeService.findAll());
  }

  private void openEditor(BackendRoute route, String title) {
    Dialog dialog = new Dialog();
    dialog.setWidth("600px");
    dialog.setHeight("500px");
    dialog.add(new Span(title));
    // TODO: Build form: Name, Path, Upstreams (multi-select), Methods (checkboxes), Priority, URL preview
    Button save = new Button("Save", e -> {
      routeService.save(route);
      dialog.close();
      loadData();
    });
    dialog.add(save);
    dialog.open();
  }
}