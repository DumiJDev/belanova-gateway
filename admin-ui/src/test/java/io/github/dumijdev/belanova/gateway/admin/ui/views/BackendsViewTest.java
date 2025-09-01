package io.github.dumijdev.belanova.gateway.admin.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import io.github.dumijdev.belanova.gateway.admin.ui.services.BackendService;
import org.mockito.Mockito;

import io.github.dumijdev.belanova.gateway.admin.ui.services.TranslationService;

class BackendsViewTest {
    @Test
    void testGridIsPresent() {
        BackendService backendService = Mockito.mock(BackendService.class);
        BackendsView view = new BackendsView(backendService);

        // The grid is wrapped in a VerticalLayout returned by getContent()
        // We need to check recursively for Grid components
        boolean hasGrid = hasGridComponent(view);
        assertTrue(hasGrid, "BackendsView should contain a Grid");
    }

    private boolean hasGridComponent(Component component) {
        if (component instanceof Grid) {
            return true;
        }

        // Check children recursively
        for (Component child : component.getChildren().toList()) {
            if (hasGridComponent(child)) {
                return true;
            }
        }

        return false;
    }
}
