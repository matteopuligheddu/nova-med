package it.nova.novamed.mapper;

import it.nova.novamed.dto.doctor.CreateDoctorRequest;
import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.model.Doctor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoctorMapperTest {

    private final DoctorMapper mapper = new DoctorMapper();

    @Test
    void toDTO_ok() {
        Doctor d = new Doctor();
        d.setId(1L);
        d.setFirstName("Mario");
        d.setLastName("Rossi");
        d.setSpecialization("Cardiology");

        DoctorDto dto = mapper.toDTO(d);

        assertEquals(1L, dto.getId());
        assertEquals("Mario", dto.getFirstName());
        assertEquals("Rossi", dto.getLastName());
        assertEquals("Cardiology", dto.getSpecialization());
    }

    @Test
    void toEntity_ok() {
        CreateDoctorRequest req = new CreateDoctorRequest();
        req.setFirstName("Giulia");
        req.setLastName("Bianchi");
        req.setSpecialization("Dermatology");

        Doctor d = mapper.toEntity(req);

        assertEquals("Giulia", d.getFirstName());
        assertEquals("Bianchi", d.getLastName());
        assertEquals("Dermatology", d.getSpecialization());
    }
}