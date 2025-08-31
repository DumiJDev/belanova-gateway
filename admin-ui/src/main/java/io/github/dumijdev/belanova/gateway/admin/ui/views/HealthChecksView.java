package io.github.dumijdev.belanova.gateway.admin.ui.views;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.dumijdev.belanova.gateway.admin.ui.layouts.MainLayout;

import java.util.List;
import java.util.Arrays;

@PageTitle("Health Checks")
@Route(value = "health-checks", layout = MainLayout.class)
public class HealthChecksView extends VerticalLayout {
    public HealthChecksView() {
        add(new H1("Health Checks"));
        Grid<Backend> grid = new Grid<>(Backend.class, false);
        grid.addColumn(Backend::getId).setHeader("Backend ID");
        grid.addColumn(Backend::getName).setHeader("Name");
        grid.addComponentColumn(this::healthStatusIndicator).setHeader("Health Status");
        // Demo data
        List<Backend> demo = Arrays.asList(
            new Backend("b1", "User Service", "User backend", "http://localhost:9001", "user-service", "/api/v1", "Healthy", true, false, null, null, null),
            new Backend("b2", "Order Service", "Order backend", "http://localhost:9002", "order-service", "/api/v1", "Unhealthy", false, false, null, null, null)
        );
        grid.setItems(demo);
        add(grid);
    }

    private Span healthStatusIndicator(Backend backend) {
        Span status = new Span();
        if (!backend.isEnabled()) {
            status.setText("Disabled");
            status.getStyle().set("color", "gray");
        } else {
            // Demo: alternate healthy/unhealthy
            if (backend.getId().endsWith("1")) {
                status.setText("Healthy");
                status.getStyle().set("color", "green");
            } else {
                status.setText("Unhealthy");
                status.getStyle().set("color", "red");
            }
        }
        return status;
    }
}
