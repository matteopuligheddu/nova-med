package it.nova.novamed.repository;

import it.nova.novamed.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static it.nova.novamed.model.AppointmentStatus.ACCEPTED;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository repo;

    @Autowired
    private TestEntityManager em;

    private Appointment createAppointment(Long ignored, Instant start, Instant end, AppointmentStatus status) {

        // USER DOCTOR
        User doctorUser = new User();
        doctorUser.setEmail("doc@test.com");
        doctorUser.setPassword("pass");
        doctorUser.setRole(Role.DOCTOR);
        doctorUser.setMustChangePassword(false);
        em.persist(doctorUser);

        Doctor d = new Doctor();
        d.setUser(doctorUser);
        em.persist(d);

        // USER PATIENT
        User patientUser = new User();
        patientUser.setEmail("pat@test.com");
        patientUser.setPassword("pass");
        patientUser.setRole(Role.PATIENT);
        patientUser.setMustChangePassword(false);
        em.persist(patientUser);

        Patient p = new Patient();
        p.setUser(patientUser);
        em.persist(p);

        // SERVICE TYPE
        ServiceType s = new ServiceType();
        s.setName("Test");
        s.setDescription("desc");
        s.setDurationMinutes(30);
        s.setPrice((int) 50.0);
        s.setDoctor(d);
        em.persist(s);

        // APPOINTMENT
        Appointment a = new Appointment();
        a.setDoctor(d);
        a.setPatient(p);
        a.setServiceType(s);
        a.setDate(start);
        a.setDateEnd(end);
        a.setStatus(status);

        em.persist(a);
        em.flush();

        return a;
    }

    @Test
    void overlaps_true() {
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);

        Appointment a = createAppointment(null, start, end, ACCEPTED);

        boolean result = repo.overlaps(
                a.getDoctor().getId(),   // <--- ID REALE
                start.minusSeconds(10),
                end.plusSeconds(10)
        );

        assertTrue(result);
    }

    @Test
    void overlaps_false_noOverlap() {
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);

        createAppointment(5L, start, end, ACCEPTED);

        boolean result = repo.overlaps(5L, end, end.plusSeconds(10));

        assertFalse(result);
    }

    @Test
    void overlaps_false_cancelledIgnored() {
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);

        createAppointment(5L, start, end, AppointmentStatus.CANCELLED);

        boolean result = repo.overlaps(5L, start.minusSeconds(10), end.plusSeconds(10));

        assertFalse(result);
    }

    @Test
    void overlapsExceptId_ignoresSameAppointment() {
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);

        Appointment a = createAppointment(5L, start, end, ACCEPTED);

        boolean result = repo.overlapsExceptId(a.getId(), 5L, start.minusSeconds(10), end.plusSeconds(10));

        assertFalse(result);
    }
}