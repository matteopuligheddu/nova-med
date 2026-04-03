package it.nova.novamed.mapper;

import it.nova.novamed.dto.service.CreateServiceTypeRequest;
import it.nova.novamed.dto.service.ServiceTypeDto;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.ServiceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTypeMapperTest {

    private final ServiceTypeMapper mapper = new ServiceTypeMapper();

    @Test
    void toDTO_ok() {
        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setId(1L);
        s.setName("Visita");
        s.setDescription("Controllo generale");
        s.setPrice((int) 50.0);
        s.setDurationMinutes(30);
        s.setDoctor(d);

        ServiceTypeDto dto = mapper.toDTO(s);

        assertEquals(1L, dto.getId());
        assertEquals("Visita", dto.getName());
        assertEquals("Controllo generale", dto.getDescription());
        assertEquals(50.0d, dto.getPrice().doubleValue());
        assertEquals(30, dto.getDurationMinutes());
        assertEquals(5L, dto.getDoctorId());
    }

    @Test
    void toEntity_ok() {
        CreateServiceTypeRequest req = new CreateServiceTypeRequest();
        req.setName("Eco");
        req.setDescription("Ecografia");
        req.setPrice((int) 80.0);
        req.setDurationMinutes(20);

        ServiceType s = mapper.toEntity(req);

        assertEquals("Eco", s.getName());
        assertEquals("Ecografia", s.getDescription());
        assertEquals(80.0, s.getPrice(), 0.0001);
        assertEquals(20, s.getDurationMinutes());
    }
}