package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpstreamServiceTest {
    @Test
    void testSaveAndFindAll() {
        UpstreamService service = new UpstreamService();
        Upstream u = new Upstream();
        u.setId("u1");
        service.save(u);
        assertEquals(1, service.findAll().size());
        assertEquals("u1", service.findAll().get(0).getId());
    }
    @Test
    void testDelete() {
        UpstreamService service = new UpstreamService();
        Upstream u = new Upstream();
        u.setId("u1");
        service.save(u);
        service.delete("u1");
        assertTrue(service.findAll().isEmpty());
    }
}
