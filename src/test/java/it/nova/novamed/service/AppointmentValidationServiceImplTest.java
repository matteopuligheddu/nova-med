package it.nova.novamed.service;

import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.DoctorAvailability;
import it.nova.novamed.model.ServiceType;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentValidationServiceImplTest {

    @InjectMocks
    private AppointmentValidationServiceImpl service;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorAvailabilityRepository availabilityRepo;

    // ---------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------
    private Doctor doctorWithService(ServiceType s) {
        Doctor d = new Doctor();
        d.setId(5L);
        d.setServiceTypes(List.of(s));
        return d;
    }

    private DoctorAvailability availability(LocalTime start, LocalTime end, int slot) {
        DoctorAvailability a = new DoctorAvailability();
        a.setStartTime(start);
        a.setEndTime(end);
        a.setSlotMinutes(slot);
        return a;
    }

    private Instant instantAt(LocalDate date, int hour, int minute) {
        return date.atTime(hour, minute)
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }

    // ---------------------------------------------------------
    // VALIDATE CREATION
    // ---------------------------------------------------------
    @Test
    void validateCreation_pastDate_throws() {
        Doctor d = new Doctor();
        ServiceType s = new ServiceType();

        LocalDate date = LocalDate.of(2020, 1, 1);
        Instant past = instantAt(date, 10, 0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateCreation(d, s, past));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validateCreation_serviceNotBelonging_throws() {
        Doctor d = new Doctor();
        d.setId(5L);
        d.setServiceTypes(List.of()); // vuoto

        ServiceType s = new ServiceType();

        LocalDate date = LocalDate.of(2030, 1, 1);
        Instant start = instantAt(date, 10, 0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateCreation(d, s, start));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validateCreation_noAvailability_throws() {
        ServiceType s = new ServiceType();
        s.setDurationMinutes(30);

        Doctor d = doctorWithService(s);

        LocalDate date = LocalDate.of(2030, 1, 1);
        DayOfWeek dow = date.getDayOfWeek();
        Instant start = instantAt(date, 10, 0);

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, dow))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateCreation(d, s, start));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validateCreation_outsideAvailability_throws() {
        ServiceType s = new ServiceType();
        s.setDurationMinutes(30);

        Doctor d = doctorWithService(s);

        LocalDate date = LocalDate.of(2030, 1, 1);
        DayOfWeek dow = date.getDayOfWeek();

        DoctorAvailability av = availability(LocalTime.of(10, 0), LocalTime.of(12, 0), 30);

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, dow))
                .thenReturn(Optional.of(av));

        // 9:00 è prima dell'inizio (10:00)
        Instant invalidStart = instantAt(date, 9, 0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateCreation(d, s, invalidStart));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validateCreation_slotNotAligned_throws() {
        ServiceType s = new ServiceType();
        s.setDurationMinutes(30);

        Doctor d = doctorWithService(s);

        LocalDate date = LocalDate.of(2030, 1, 1);
        DayOfWeek dow = date.getDayOfWeek();

        DoctorAvailability av = availability(LocalTime.of(10, 0), LocalTime.of(12, 0), 30);

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, dow))
                .thenReturn(Optional.of(av));

        // 10:15 non è allineato a slot da 30 minuti
        Instant invalidStart = instantAt(date, 10, 15);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateCreation(d, s, invalidStart));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validateCreation_overlapping_throws() {
        ServiceType s = new ServiceType();
        s.setDurationMinutes(30);

        Doctor d = doctorWithService(s);

        LocalDate date = LocalDate.of(2030, 1, 1);
        DayOfWeek dow = date.getDayOfWeek();
        Instant start = instantAt(date, 10, 0);

        DoctorAvailability av = availability(LocalTime.of(10, 0), LocalTime.of(12, 0), 30);

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, dow))
                .thenReturn(Optional.of(av));

        when(appointmentRepository.overlaps(eq(5L), any(), any()))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateCreation(d, s, start));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void validateCreation_ok() {
        ServiceType s = new ServiceType();
        s.setDurationMinutes(30);

        Doctor d = doctorWithService(s);

        LocalDate date = LocalDate.of(2030, 1, 1);
        DayOfWeek dow = date.getDayOfWeek();
        Instant start = instantAt(date, 10, 0); // dentro 10–12, allineato

        DoctorAvailability av = availability(LocalTime.of(10, 0), LocalTime.of(12, 0), 30);

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, dow))
                .thenReturn(Optional.of(av));

        when(appointmentRepository.overlaps(eq(5L), any(), any()))
                .thenReturn(false);

        assertDoesNotThrow(() -> service.validateCreation(d, s, start));
    }

    // ---------------------------------------------------------
    // VALIDATE UPDATE
    // ---------------------------------------------------------
    @Test
    void validateUpdate_overlapping_throws() {
        ServiceType s = new ServiceType();
        s.setDurationMinutes(30);

        Doctor d = doctorWithService(s);

        LocalDate date = LocalDate.of(2030, 1, 1);
        DayOfWeek dow = date.getDayOfWeek();
        Instant start = instantAt(date, 10, 0);

        DoctorAvailability av = availability(LocalTime.of(10, 0), LocalTime.of(12, 0), 30);

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, dow))
                .thenReturn(Optional.of(av));

        when(appointmentRepository.overlapsExceptId(eq(99L), eq(5L), any(), any()))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validateUpdate(99L, d, s, start));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void validateUpdate_ok() {
        ServiceType s = new ServiceType();
        s.setDurationMinutes(30);

        Doctor d = doctorWithService(s);

        LocalDate date = LocalDate.of(2030, 1, 1);
        DayOfWeek dow = date.getDayOfWeek();
        Instant start = instantAt(date, 10, 0);

        DoctorAvailability av = availability(LocalTime.of(10, 0), LocalTime.of(12, 0), 30);

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, dow))
                .thenReturn(Optional.of(av));

        when(appointmentRepository.overlapsExceptId(eq(99L), eq(5L), any(), any()))
                .thenReturn(false);

        assertDoesNotThrow(() -> service.validateUpdate(99L, d, s, start));
    }
}