package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Plugins")
@Route(value = "plugins", layout = MainLayout.class)
public class PluginsView extends VerticalLayout {
    private final List<String> pluginList = new ArrayList<>();
    private final Grid<String> grid = new Grid<>(String.class, false);

    public PluginsView() {
        add(new H1("Plugins"));
        grid.addColumn(String::toString).setHeader("Plugin Name");
        grid.setItems(pluginList);

        Button addButton = new Button("Add Plugin", e -> openPluginDialog());
        Button deleteButton = new Button("Delete Selected", e -> {
            String selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                pluginList.remove(selected);
                grid.setItems(pluginList);
            } else Notification.show("Select a plugin to delete");
        });
        HorizontalLayout actions = new HorizontalLayout(addButton, deleteButton);
        add(actions, grid);
    }

    private void openPluginDialog() {
        Dialog dialog = new Dialog();
        TextField nameField = new TextField("Plugin Name");
        Button save = new Button("Save", e -> {
            String name = nameField.getValue();
            if (name != null && !name.isBlank()) {
                pluginList.add(name);
                grid.setItems(pluginList);
                dialog.close();
            } else {
                Notification.show("Plugin name required");
            }
        });
        FormLayout form = new FormLayout(nameField, save);
        dialog.add(form);
        dialog.open();
    }
}
