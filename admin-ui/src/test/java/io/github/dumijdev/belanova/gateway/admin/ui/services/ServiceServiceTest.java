package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.common.model.Service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServiceServiceTest {
    @Test
    void testSaveAndFindAll() {
        ServiceService service = new ServiceService();
        Service s = new Service();
        s.setId("s1");
        service.save(s);
        assertEquals(1, service.findAll().size());
        assertEquals("s1", service.findAll().get(0).getId());
    }
    @Test
    void testDelete() {
        ServiceService service = new ServiceService();
        Service s = new Service();
        s.setId("s1");
        service.save(s);
        service.delete("s1");
        assertTrue(service.findAll().isEmpty());
    }
}
