package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.CreateDoctorAvailabilityRequest;
import it.nova.novamed.dto.doctor.DoctorAvailabilityDto;
import it.nova.novamed.dto.doctor.UpdateDoctorAvailabilityRequest;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.DoctorAvailabilityMapper;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.DoctorAvailability;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import it.nova.novamed.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceImplTest {

    @InjectMocks
    private DoctorAvailabilityServiceImpl service;

    @Mock
    private DoctorAvailabilityRepository availabilityRepo;

    @Mock
    private DoctorRepository doctorRepo;

    @Mock
    private AdminServiceImpl adminService;

    @Mock
    private DoctorAvailabilityMapper mapper;

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @Test
    void create_admin_createsAvailability() {
        CreateDoctorAvailabilityRequest req = new CreateDoctorAvailabilityRequest();
        req.setDayOfWeek(DayOfWeek.MONDAY);
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(12, 0));
        req.setSlotMinutes(30);

        Doctor doctor = new Doctor();
        doctor.setId(5L);

        DoctorAvailability saved = new DoctorAvailability();
        DoctorAvailabilityDto dto = new DoctorAvailabilityDto();

        when(adminService.isAdmin(1L)).thenReturn(true);
        when(doctorRepo.findById(5L)).thenReturn(Optional.of(doctor));
        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());
        when(availabilityRepo.save(any())).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dto);

        DoctorAvailabilityDto result = service.create(1L, 5L, req);

        assertSame(dto, result);
    }

    @Test
    void create_doctorOwner_createsAvailability() {
        CreateDoctorAvailabilityRequest req = new CreateDoctorAvailabilityRequest();
        req.setDayOfWeek(DayOfWeek.MONDAY);

        Doctor doctor = new Doctor();
        doctor.setId(5L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepo.findById(5L)).thenReturn(Optional.of(doctor));
        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());

        when(availabilityRepo.save(any())).thenReturn(new DoctorAvailability());
        when(mapper.toDto(any())).thenReturn(new DoctorAvailabilityDto());

        assertDoesNotThrow(() -> service.create(1L, 5L, req));
    }

    @Test
    void create_doctorNotOwner_throws() {
        CreateDoctorAvailabilityRequest req = new CreateDoctorAvailabilityRequest();
        req.setDayOfWeek(DayOfWeek.MONDAY);

        Doctor doctor = new Doctor();
        doctor.setId(99L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(doctor));

        assertThrows(UnauthorizedException.class, () -> service.create(1L, 5L, req));
    }

    @Test
    void create_userNotAdminOrDoctor_throws() {
        CreateDoctorAvailabilityRequest req = new CreateDoctorAvailabilityRequest();

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.create(1L, 5L, req));
    }

    @Test
    void create_doctorNotFound_throws() {
        CreateDoctorAvailabilityRequest req = new CreateDoctorAvailabilityRequest();

        when(adminService.isAdmin(1L)).thenReturn(true);
        when(doctorRepo.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(1L, 5L, req));
    }

    @Test
    void create_conflictAvailability_throws409() {
        CreateDoctorAvailabilityRequest req = new CreateDoctorAvailabilityRequest();
        req.setDayOfWeek(DayOfWeek.MONDAY);

        Doctor doctor = new Doctor();
        doctor.setId(5L);

        when(adminService.isAdmin(1L)).thenReturn(true);
        when(doctorRepo.findById(5L)).thenReturn(Optional.of(doctor));
        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(new DoctorAvailability()));

        assertThrows(ResponseStatusException.class, () -> service.create(1L, 5L, req));
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @Test
    void update_admin_updatesAvailability() {
        UpdateDoctorAvailabilityRequest req = new UpdateDoctorAvailabilityRequest();
        req.setDayOfWeek(DayOfWeek.TUESDAY);
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(12, 0));
        req.setSlotMinutes(20);

        Doctor doctor = new Doctor();
        doctor.setId(5L);

        DoctorAvailability existing = new DoctorAvailability();
        existing.setDoctor(doctor);

        when(availabilityRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(adminService.isAdmin(1L)).thenReturn(true);
        when(availabilityRepo.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(new DoctorAvailabilityDto());

        DoctorAvailabilityDto result = service.update(1L, 10L, req);

        assertNotNull(result);
        assertEquals(DayOfWeek.TUESDAY, existing.getDayOfWeek());
    }

    @Test
    void update_doctorNotOwner_throws() {
        UpdateDoctorAvailabilityRequest req = new UpdateDoctorAvailabilityRequest();

        Doctor doctor = new Doctor();
        doctor.setId(99L);

        DoctorAvailability existing = new DoctorAvailability();
        existing.setDoctor(doctor);

        when(availabilityRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(new Doctor()));

        assertThrows(UnauthorizedException.class, () -> service.update(1L, 10L, req));
    }

    @Test
    void update_notFound_throws() {
        UpdateDoctorAvailabilityRequest req = new UpdateDoctorAvailabilityRequest();

        when(availabilityRepo.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, 10L, req));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Test
    void delete_admin_deletes() {
        Doctor doctor = new Doctor();
        doctor.setId(5L);

        DoctorAvailability existing = new DoctorAvailability();
        existing.setDoctor(doctor);

        when(availabilityRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(adminService.isAdmin(1L)).thenReturn(true);

        assertDoesNotThrow(() -> service.delete(1L, 10L));
        verify(availabilityRepo).delete(existing);
    }

    @Test
    void delete_doctorNotOwner_throws() {
        Doctor doctor = new Doctor();
        doctor.setId(99L);

        DoctorAvailability existing = new DoctorAvailability();
        existing.setDoctor(doctor);

        when(availabilityRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(new Doctor()));

        assertThrows(UnauthorizedException.class, () -> service.delete(1L, 10L));
    }

    @Test
    void delete_notFound_throws() {
        when(availabilityRepo.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L, 10L));
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR
    // ---------------------------------------------------------
    @Test
    void getByDoctor_admin_ok() {
        DoctorAvailability a = new DoctorAvailability();
        DoctorAvailabilityDto dto = new DoctorAvailabilityDto();

        when(adminService.isAdmin(1L)).thenReturn(true);
        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of(a));
        when(mapper.toDto(a)).thenReturn(dto);

        List<DoctorAvailabilityDto> result = service.getByDoctor(1L, 5L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getByDoctor_doctorOwner_ok() {
        Doctor doctor = new Doctor();
        doctor.setId(5L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepo.findByDoctorId(5L)).thenReturn(List.of());

        List<DoctorAvailabilityDto> result = service.getByDoctor(1L, 5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByDoctor_doctorNotOwner_throws() {
        Doctor doctor = new Doctor();
        doctor.setId(99L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepo.findByUser_Id(1L)).thenReturn(Optional.of(doctor));

        assertThrows(UnauthorizedException.class, () -> service.getByDoctor(1L, 5L));
    }
}