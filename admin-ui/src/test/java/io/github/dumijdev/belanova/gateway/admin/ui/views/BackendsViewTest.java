package io.github.dumijdev.belanova.gateway.admin.ui.views;

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
        BackendsView view = new BackendsView(backendService, Mockito.mock(TranslationService.class));
        boolean hasGrid = view.getChildren().anyMatch(c -> c instanceof Grid);
        assertTrue(hasGrid, "BackendsView should contain a Grid");
    }
}
