package io.github.dumijdev.belanova.gateway.admin.ui.views;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.UpstreamService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@PageTitle("Upstreams")
@Route(value = "upstreams", layout = MainLayout.class)
public class UpstreamsView extends VerticalLayout {
    private final UpstreamService upstreamService;
    private final Grid<Upstream> grid = new Grid<>(Upstream.class, false);

    @Autowired
    public UpstreamsView(UpstreamService upstreamService) {
        this.upstreamService = upstreamService;
        addClassName("upstreams-view");
        setSizeFull();
        setPadding(false);
        add(createHeader(), createGrid());
        refreshGrid();
    }

    private HorizontalLayout createHeader() {
        Span title = new Span("Upstreams");
        title.addClassName("view-title");
        Button addButton = new Button("Add Upstream", VaadinIcon.PLUS.create(), e -> openUpstreamDialog(new Upstream()));
        addButton.addClassName("primary-action");
        HorizontalLayout header = new HorizontalLayout(title, addButton);
        header.addClassName("view-header");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("justify-content", "space-between");
        return header;
    }

    private Grid<Upstream> createGrid() {
        grid.addClassName("upstreams-grid");
        grid.addColumn(Upstream::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Upstream::getHost).setHeader("Host").setSortable(true).setAutoWidth(true);
        grid.addColumn(Upstream::getPort).setHeader("Port").setAutoWidth(true);
        grid.addColumn(Upstream::isEnabled).setHeader("Enabled").setAutoWidth(true);

        Button editButton = new Button("Edit Selected");
        editButton.addClassName("action-secondary");
        Button deleteButton = new Button("Delete Selected");
        deleteButton.addClassName("action-danger");
        HorizontalLayout actions = getActions(upstreamService, editButton, deleteButton);
        actions.addClassName("actions-row");
        add(actions);

        grid.setWidthFull();
        grid.setHeight("60vh");
        return grid;
    }

    @NotNull
    private HorizontalLayout getActions(UpstreamService upstreamService, Button editButton, Button deleteButton) {
        editButton.addClickListener(e -> {
            Upstream selected = grid.asSingleSelect().getValue();
            if (selected != null) openUpstreamDialog(selected);
            else Notification.show("Select an upstream to edit");
        });
        deleteButton.addClickListener(e -> {
            Upstream selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                upstreamService.delete(selected.getId());
                refreshGrid();
            } else Notification.show("Select an upstream to delete");
        });
        return new HorizontalLayout(editButton, deleteButton);
    }

    private void refreshGrid() {
        List<Upstream> all = upstreamService.findAll();
        grid.setItems(all);
    }

    private void openUpstreamDialog(Upstream upstream) {
        Dialog dialog = new Dialog();
        Binder<Upstream> binder = new Binder<>(Upstream.class);
        TextField idField = new TextField("ID");
        TextField hostField = new TextField("Host");
        IntegerField portField = new IntegerField("Port");
        binder.bind(idField, Upstream::getId, Upstream::setId);
        binder.bind(hostField, Upstream::getHost, Upstream::setHost);
        binder.bind(portField, Upstream::getPort, Upstream::setPort);
        binder.readBean(upstream);
        Button save = new Button("Save", e -> {
            try {
                binder.writeBean(upstream);
                upstreamService.save(upstream);
                refreshGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Validation error: " + ex.getMessage());
            }
        });
        FormLayout form = new FormLayout(idField, hostField, portField, save);
        dialog.add(form);
        dialog.open();
    }
}
