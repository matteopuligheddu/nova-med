package it.nova.novamed.mapper;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentMapperTest {

    private final DoctorMapper doctorMapper = new DoctorMapper();
    private final ServiceTypeMapper serviceTypeMapper = new ServiceTypeMapper();

    private final AppointmentMapper mapper =
            new AppointmentMapper(doctorMapper, serviceTypeMapper);


    @Test
    void toDTO_ok() {
        Patient p = new Patient();
        p.setId(10L);
        p.setFirstName("Mario");
        p.setLastName("Rossi");

        Doctor d = new Doctor();
        d.setId(20L);
        d.setFirstName("Giulia");
        d.setLastName("Bianchi");

        ServiceType s = new ServiceType();
        s.setId(30L);

        Appointment a = new Appointment();
        a.setId(1L);
        a.setDate(Instant.now());
        a.setDateEnd(Instant.now().plusSeconds(3600));
        a.setStatus(AppointmentStatus.ACCEPTED);
        a.setPatient(p);
        a.setDoctor(d);
        a.setServiceType(s);
        a.setNotes("Test notes");

        AppointmentDto dto = mapper.toDTO(a);

        assertEquals(1L, dto.getId());
        assertEquals(a.getDate(), dto.getDate());
        assertEquals(a.getDateEnd(), dto.getDateEnd());
        assertEquals(AppointmentStatus.ACCEPTED, dto.getStatus());
        assertEquals(10L, dto.getPatientId());
        assertEquals(20L, dto.getDoctorId());
        assertEquals(30L, dto.getServiceTypeId());
        assertEquals("Mario Rossi", dto.getPatientName());
        assertEquals("Giulia Bianchi", dto.getDoctorName());
        assertEquals("Test notes", dto.getNotes());
    }
}