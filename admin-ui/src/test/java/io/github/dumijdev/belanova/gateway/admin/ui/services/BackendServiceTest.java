package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BackendServiceTest {
    @Test
    void testSaveAndFindAll() {
        BackendService service = new BackendService();
        Backend b = new Backend();
        b.setId("b1");
        service.save(b);
        assertEquals(1, service.findAll().size());
        assertEquals("b1", service.findAll().get(0).getId());
    }
    @Test
    void testDelete() {
        BackendService service = new BackendService();
        Backend b = new Backend();
        b.setId("b1");
        service.save(b);
        service.delete("b1");
        assertTrue(service.findAll().isEmpty());
    }
}
