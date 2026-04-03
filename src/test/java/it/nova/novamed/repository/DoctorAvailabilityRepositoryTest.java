package it.nova.novamed.repository;

import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.DoctorAvailability;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DoctorAvailabilityRepositoryTest {

    @Autowired
    private DoctorAvailabilityRepository repo;

    @Autowired
    private TestEntityManager em;

    @Test
    void findByDoctorIdAndDayOfWeek_ok() {
        Doctor d = new Doctor();
        em.persist(d);

        DoctorAvailability a = new DoctorAvailability();
        a.setDoctor(d);
        a.setDayOfWeek(DayOfWeek.MONDAY);
        a.setStartTime(LocalTime.of(9, 0));
        a.setEndTime(LocalTime.of(17, 0));
        a.setSlotMinutes(30);

        em.persist(a);

        Optional<DoctorAvailability> result =
                repo.findByDoctorIdAndDayOfWeek(d.getId(), DayOfWeek.MONDAY);

        assertTrue(result.isPresent());
        assertEquals(LocalTime.of(9, 0), result.get().getStartTime());
    }

    @Test
    void findByDoctorIdAndDayOfWeek_notFound() {
        Optional<DoctorAvailability> result =
                repo.findByDoctorIdAndDayOfWeek(999L, DayOfWeek.MONDAY);

        assertTrue(result.isEmpty());
    }
}