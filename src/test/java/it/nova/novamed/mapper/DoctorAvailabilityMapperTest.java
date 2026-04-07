package it.nova.novamed.mapper;

import it.nova.novamed.dto.doctor.DoctorAvailabilityDto;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.DoctorAvailability;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DoctorAvailabilityMapperTest {

    private final DoctorAvailabilityMapper mapper = new DoctorAvailabilityMapper();

    @Test
    void toDto_ok() {
        Doctor d = new Doctor();
        d.setId(5L);

        DoctorAvailability a = new DoctorAvailability();
        a.setId(1L);
        a.setDoctor(d);
        a.setDayOfWeek(DayOfWeek.MONDAY);
        a.setStartTime(LocalTime.of(9, 0));
        a.setEndTime(LocalTime.of(17, 0));

        DoctorAvailabilityDto dto = mapper.toDto(a);

        assertEquals(1L, dto.getId());
        assertEquals(5L, dto.getDoctorId());
        assertEquals(DayOfWeek.MONDAY, dto.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), dto.getStartTime());
        assertEquals(LocalTime.of(17, 0), dto.getEndTime());
    }
}