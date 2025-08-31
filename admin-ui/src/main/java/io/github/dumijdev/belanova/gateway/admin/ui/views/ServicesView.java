package io.github.dumijdev.belanova.gateway.admin.ui.views;

import io.github.dumijdev.belanova.gateway.common.model.Service;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;
import io.github.dumijdev.belanova.gateway.admin.ui.services.ServiceService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@PageTitle("Services")
@Route(value = "services", layout = MainLayout.class)
@CssImport("./styles/services.css")
public class ServicesView extends VerticalLayout {
    private final ServiceService serviceService;
    private final Grid<Service> grid = new Grid<>(Service.class, false);

    @Autowired
    public ServicesView(ServiceService serviceService) {
        this.serviceService = serviceService;
        addClassName("services-view");
        setSizeFull();
        setPadding(false);

        add(createHeader(), createGrid());
        refreshGrid();
    }

    private HorizontalLayout createHeader() {
        Span title = new Span("Services");
        title.addClassName("view-title");
        Button addButton = new Button("Add Service", VaadinIcon.PLUS.create(), e -> openServiceDialog(new Service()));
        addButton.addClassName("primary-action");
        HorizontalLayout header = new HorizontalLayout(title, addButton);
        header.addClassName("view-header");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("justify-content", "space-between");
        return header;
    }

    private Grid<Service> createGrid() {
        grid.addClassName("services-grid");
        grid.addColumn(Service::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Service::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Service::getPath).setHeader("Path").setFlexGrow(1);
        grid.addColumn(Service::isEnabled).setHeader("Enabled").setAutoWidth(true);

        Button editButton = new Button("Edit Selected");
        editButton.addClassName("action-secondary");
        Button deleteButton = new Button("Delete Selected");
        deleteButton.addClassName("action-danger");

        HorizontalLayout actions = getActions(serviceService, editButton, deleteButton);
        actions.addClassName("actions-row");
        add(actions);

        grid.setWidthFull();
        grid.setHeight("60vh");
        return grid;
    }

    @NotNull
    private HorizontalLayout getActions(ServiceService serviceService, Button editButton, Button deleteButton) {
        editButton.addClickListener(e -> {
            Service selected = grid.asSingleSelect().getValue();
            if (selected != null) openServiceDialog(selected);
            else Notification.show("Select a service to edit");
        });
        deleteButton.addClickListener(e -> {
            Service selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                serviceService.delete(selected.getId());
                refreshGrid();
            } else Notification.show("Select a service to delete");
        });
        return new HorizontalLayout(editButton, deleteButton);
    }

    private void refreshGrid() {
        List<Service> all = serviceService.findAll();
        grid.setItems(all);
    }

    private void openServiceDialog(Service service) {
        Dialog dialog = new Dialog();
        Binder<Service> binder = new Binder<>(Service.class);
        TextField idField = new TextField("ID");
        TextField nameField = new TextField("Name");
        TextField pathField = new TextField("Path");
        binder.bind(idField, Service::getId, Service::setId);
        binder.bind(nameField, Service::getName, Service::setName);
        binder.bind(pathField, Service::getPath, Service::setPath);
        binder.readBean(service);
        Button save = new Button("Save", e -> {
            try {
                binder.writeBean(service);
                serviceService.save(service);
                refreshGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Validation error: " + ex.getMessage());
            }
        });
        FormLayout form = new FormLayout(idField, nameField, pathField, save);
        dialog.add(form);
        dialog.open();
    }
}
