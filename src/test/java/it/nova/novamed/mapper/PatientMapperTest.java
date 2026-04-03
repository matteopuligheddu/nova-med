package it.nova.novamed.mapper;

import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.model.Gender;
import it.nova.novamed.model.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PatientMapperTest {

    private final PatientMapper mapper = new PatientMapper();

    @Test
    void toDTO_ok() {
        Patient p = new Patient();
        p.setId(1L);
        p.setFirstName("Mario");
        p.setLastName("Rossi");
        p.setPhoneNumber("123");
        p.setBirthDate(LocalDate.of(1990, 1, 1));
        p.setGender(Gender.MALE);

        PatientDto dto = mapper.toDTO(p);

        assertEquals(1L, dto.getId());
        assertEquals("Mario", dto.getFirstName());
        assertEquals("Rossi", dto.getLastName());
        assertEquals("123", dto.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 1), dto.getBirthDate());
        assertEquals(Gender.MALE, dto.getGender());
    }

    @Test
    void toEntity_ok() {
        CreatePatientRequest req = new CreatePatientRequest();
        req.setFirstName("Giulia");
        req.setLastName("Bianchi");
        req.setPhoneNumber("555");
        req.setBirthDate(LocalDate.of(2000, 5, 10));
        req.setGender(Gender.FEMALE);

        Patient p = mapper.toEntity(req);

        assertEquals("Giulia", p.getFirstName());
        assertEquals("Bianchi", p.getLastName());
        assertEquals("555", p.getPhoneNumber());
        assertEquals(LocalDate.of(2000, 5, 10), p.getBirthDate());
        assertEquals(Gender.FEMALE, p.getGender());
    }
}