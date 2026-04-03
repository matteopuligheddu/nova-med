package it.nova.novamed.service;

import it.nova.novamed.dto.calendar.SlotDto;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.ServiceTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @InjectMocks
    private SlotServiceImpl service;

    @Mock
    private DoctorAvailabilityRepository availabilityRepo;

    @Mock
    private AppointmentRepository appointmentRepo;

    @Mock
    private ServiceTypeRepository serviceRepo;

    @Mock
    private DoctorRepository doctorRepo;

    @Mock
    private AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // ACCESS CONTROL
    // ---------------------------------------------------------
    @Test
    void access_admin_allowed() {
        when(adminService.isAdmin(1L)).thenReturn(true);

        assertDoesNotThrow(() -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void access_doctorOwner_allowed() {
        Doctor d = new Doctor();
        d.setId(5L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(d));

        assertDoesNotThrow(() -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void access_doctorNotOwner_throws() {
        Doctor d = new Doctor();
        d.setId(99L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(d));

        assertThrows(UnauthorizedException.class, () -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void access_doctorNotFound_throws() {
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void access_patient_allowed() {
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(false);
        when(adminService.isPatient(1L)).thenReturn(true);

        assertDoesNotThrow(() -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void access_unknownUser_throws() {
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(false);
        when(adminService.isPatient(1L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> invokeCheckAccess(1L, 5L));
    }

    private void invokeCheckAccess(Long userId, Long doctorId) {
        try {
            var m = SlotServiceImpl.class.getDeclaredMethod("checkAccess", Long.class, Long.class);
            m.setAccessible(true);
            m.invoke(service, userId, doctorId);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
        }
    }

    // ---------------------------------------------------------
    // GENERATE SLOTS
    // ---------------------------------------------------------
    @Test
    void generateSlots_noAvailability_returnsEmpty() {
        mockAccessOk();

        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of());

        List<SlotDto> result = service.generateSlots(
                1L, 5L, LocalDate.of(2025, 1, 6), 10L
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void generateSlots_serviceTypeNotFound_throws() {
        mockAccessOk();

        DoctorAvailability av = new DoctorAvailability();
        av.setDayOfWeek(DayOfWeek.MONDAY);

        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of(av));
        when(serviceRepo.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.generateSlots(1L, 5L, LocalDate.of(2025, 1, 6), 10L)
        );
    }

    @Test
    void generateSlots_doctorNotFound_throws() {
        mockAccessOk();

        DoctorAvailability av = new DoctorAvailability();
        av.setDayOfWeek(DayOfWeek.MONDAY);

        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of(av));
        when(serviceRepo.findById(10L)).thenReturn(Optional.of(new ServiceType()));
        when(doctorRepo.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.generateSlots(1L, 5L, LocalDate.of(2025, 1, 6), 10L)
        );
    }

    @Test
    void generateSlots_serviceNotBelongingToDoctor_throws() {
        mockAccessOk();

        DoctorAvailability av = new DoctorAvailability();
        av.setDayOfWeek(DayOfWeek.MONDAY);

        ServiceType s = new ServiceType();
        s.setId(10L);

        Doctor d = new Doctor();
        d.setId(5L);
        d.setServiceTypes(List.of()); // non contiene s

        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of(av));
        when(serviceRepo.findById(10L)).thenReturn(Optional.of(s));
        when(doctorRepo.findById(5L)).thenReturn(Optional.of(d));

        assertThrows(UnauthorizedException.class, () ->
                service.generateSlots(1L, 5L, LocalDate.of(2025, 1, 6), 10L)
        );
    }

    @Test
    void generateSlots_generatesFreeSlots() {
        mockAccessOk();

        DoctorAvailability av = new DoctorAvailability();
        av.setDayOfWeek(DayOfWeek.MONDAY);
        av.setStartTime(LocalTime.of(9, 0));
        av.setEndTime(LocalTime.of(10, 0));
        av.setSlotMinutes(30);

        ServiceType s = new ServiceType();
        s.setId(10L);
        s.setDurationMinutes(30);

        Doctor d = new Doctor();
        d.setId(5L);
        d.setServiceTypes(List.of(s));

        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of(av));
        when(serviceRepo.findById(10L)).thenReturn(Optional.of(s));
        when(doctorRepo.findById(5L)).thenReturn(Optional.of(d));
        when(appointmentRepo.overlaps(any(), any(), any())).thenReturn(false);

        List<SlotDto> result = service.generateSlots(
                1L, 5L, LocalDate.of(2025, 1, 6), 10L
        );

        assertEquals(2, result.size());
        assertTrue(result.get(0).free());
    }

    @Test
    void generateSlots_slotOccupied_markedNotFree() {
        mockAccessOk();

        DoctorAvailability av = new DoctorAvailability();
        av.setDayOfWeek(DayOfWeek.MONDAY);
        av.setStartTime(LocalTime.of(9, 0));
        av.setEndTime(LocalTime.of(10, 0));
        av.setSlotMinutes(30);

        ServiceType s = new ServiceType();
        s.setId(10L);
        s.setDurationMinutes(30);

        Doctor d = new Doctor();
        d.setId(5L);
        d.setServiceTypes(List.of(s));

        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of(av));
        when(serviceRepo.findById(10L)).thenReturn(Optional.of(s));
        when(doctorRepo.findById(5L)).thenReturn(Optional.of(d));
        when(appointmentRepo.overlaps(any(), any(), any())).thenReturn(true);

        List<SlotDto> result = service.generateSlots(
                1L, 5L, LocalDate.of(2025, 1, 6), 10L
        );

        assertFalse(result.get(0).free());
    }

    // ---------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------
    private void mockAccessOk() {
        when(adminService.isAdmin(any())).thenReturn(true);
    }
}