package it.nova.novamed.service;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.dto.appointment.AppointmentRequest;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.AppointmentMapper;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @InjectMocks
    private AppointmentServiceImpl service;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentMapper mapper;

    @Mock
    private AppointmentValidationServiceImpl validationService;

    @Mock
    private AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    @Test
    void getAll_admin_returnsList() {
        Mockito.doNothing().when(adminService).checkAdmin(1L);

        Appointment a = new Appointment();
        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findAll()).thenReturn(List.of(a));
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        List<AppointmentDto> result = service.getAll(1L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getAll_notAdmin_throws() {
        Mockito.doThrow(new UnauthorizedException("not admin"))
                .when(adminService).checkAdmin(2L);

        assertThrows(UnauthorizedException.class, () -> service.getAll(2L));
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @Test
    void getById_admin_returnsDto() {
        Appointment a = new Appointment();
        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isAdmin(1L)).thenReturn(true);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.getById(1L, 10L);

        assertSame(dto, result);
    }

    @Test
    void getById_doctor_checksOwnership() {
        Appointment a = new Appointment();
        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isAdmin(1L)).thenReturn(false);
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);

        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 10L);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.getById(1L, 10L);

        assertSame(dto, result);
    }

    @Test
    void getById_patient_checksOwnership() {
        Appointment a = new Appointment();
        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isAdmin(1L)).thenReturn(false);
        Mockito.when(adminService.isDoctor(1L)).thenReturn(false);
        Mockito.when(adminService.isPatient(1L)).thenReturn(true);

        Mockito.doNothing().when(adminService).checkPatientOwnsAppointment(1L, 10L);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.getById(1L, 10L);

        assertSame(dto, result);
    }

    @Test
    void getById_notFound_throws() {
        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L, 10L));
    }

    @Test
    void getById_unknownRole_throws() {
        Appointment a = new Appointment();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isAdmin(1L)).thenReturn(false);
        Mockito.when(adminService.isDoctor(1L)).thenReturn(false);
        Mockito.when(adminService.isPatient(1L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.getById(1L, 10L));
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @Test
    void create_patient_createsAppointment() {
        AppointmentRequest req = new AppointmentRequest();
        req.setDoctorId(5L);
        req.setServiceTypeId(7L);
        req.setDate(LocalDate.of(2025, 1, 1));
        req.setTime(LocalTime.of(10, 0));

        Patient p = new Patient();
        Doctor d = new Doctor();
        ServiceType s = new ServiceType();
        s.setId(7L);
        s.setDurationMinutes(30);
        d.setServiceTypes(List.of(s));

        Appointment saved = new Appointment();
        AppointmentDto dto = new AppointmentDto();

        Mockito.doNothing().when(adminService).checkPatient(1L);
        Mockito.when(patientRepository.findByUser_Id(1L)).thenReturn(Optional.of(p));
        Mockito.when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));

        Mockito.doNothing().when(validationService)
                .validateCreation(Mockito.eq(d), Mockito.eq(s), Mockito.any());

        Mockito.when(appointmentRepository.save(Mockito.any())).thenReturn(saved);
        Mockito.when(mapper.toDTO(saved)).thenReturn(dto);

        AppointmentDto result = service.create(1L, req);

        assertSame(dto, result);
    }

    @Test
    void create_notPatient_throws() {
        AppointmentRequest req = new AppointmentRequest();

        Mockito.doThrow(new UnauthorizedException("not patient"))
                .when(adminService).checkPatient(2L);

        assertThrows(UnauthorizedException.class, () -> service.create(2L, req));
    }

    @Test
    void create_patientNotFound_throws() {
        AppointmentRequest req = new AppointmentRequest();

        Mockito.doNothing().when(adminService).checkPatient(1L);
        Mockito.when(patientRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(1L, req));
    }

    @Test
    void create_doctorNotFound_throws() {
        AppointmentRequest req = new AppointmentRequest();

        Mockito.doNothing().when(adminService).checkPatient(1L);
        Mockito.when(patientRepository.findByUser_Id(1L)).thenReturn(Optional.of(new Patient()));
        Mockito.when(doctorRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(1L, req));
    }

    @Test
    void create_serviceTypeNotFound_throws() {
        AppointmentRequest req = new AppointmentRequest();
        req.setServiceTypeId(99L);

        Doctor d = new Doctor();
        d.setServiceTypes(List.of());

        Mockito.doNothing().when(adminService).checkPatient(1L);
        Mockito.when(patientRepository.findByUser_Id(1L)).thenReturn(Optional.of(new Patient()));
        Mockito.when(doctorRepository.findById(Mockito.any())).thenReturn(Optional.of(d));

        assertThrows(ResourceNotFoundException.class, () -> service.create(1L, req));
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @Test
    void update_updatesAppointment() {
        Appointment a = new Appointment();
        a.setId(10L);

        AppointmentRequest req = new AppointmentRequest();
        req.setDoctorId(5L);
        req.setServiceTypeId(7L);
        req.setDate(LocalDate.of(2025, 1, 1));
        req.setTime(LocalTime.of(10, 0));

        Doctor d = new Doctor();
        ServiceType s = new ServiceType();
        s.setId(7L);
        s.setDurationMinutes(30);
        d.setServiceTypes(List.of(s));

        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 10L);

        Mockito.when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        Mockito.doNothing().when(validationService)
                .validateUpdate(Mockito.eq(10L), Mockito.eq(d), Mockito.eq(s), Mockito.any());

        Mockito.when(appointmentRepository.save(a)).thenReturn(a);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.update(1L, 10L, req);

        assertSame(dto, result);
    }

    // ---------------------------------------------------------
    // CANCEL
    // ---------------------------------------------------------
    @Test
    void cancel_setsCancelled() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.BOOKED);

        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 10L);

        Mockito.when(appointmentRepository.save(a)).thenReturn(a);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.cancel(1L, 10L);

        assertSame(dto, result);
        assertEquals(AppointmentStatus.CANCELLED, a.getStatus());
    }

    @Test
    void cancel_alreadyCancelled_throws() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.CANCELLED);

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.cancel(1L, 10L));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Test
    void delete_admin_deletes() {
        Mockito.doNothing().when(adminService).checkAdmin(1L);
        Mockito.when(appointmentRepository.existsById(10L)).thenReturn(true);

        service.delete(1L, 10L);

        Mockito.verify(appointmentRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throws() {
        Mockito.doNothing().when(adminService).checkAdmin(1L);
        Mockito.when(appointmentRepository.existsById(10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L, 10L));
    }

    // ---------------------------------------------------------
    // GET BY PATIENT
    // ---------------------------------------------------------
    @Test
    void getByPatient_patient_checksOwnership() {
        Patient p = new Patient();
        p.setId(5L);

        Appointment a = new Appointment();
        AppointmentDto dto = new AppointmentDto();

        Mockito.when(patientRepository.findById(5L)).thenReturn(Optional.of(p));
        Mockito.when(adminService.isPatient(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkPatientOwnsAppointment(1L, 5L);

        Mockito.when(appointmentRepository.findByPatientId(5L)).thenReturn(List.of(a));
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        List<AppointmentDto> result = service.getByPatient(1L, 5L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getByPatient_admin_allowed() {
        Patient p = new Patient();
        p.setId(5L);

        Mockito.when(patientRepository.findById(5L)).thenReturn(Optional.of(p));
        Mockito.when(adminService.isPatient(1L)).thenReturn(false);
        Mockito.doNothing().when(adminService).checkAdmin(1L);

        Mockito.when(appointmentRepository.findByPatientId(5L)).thenReturn(List.of());

        List<AppointmentDto> result = service.getByPatient(1L, 5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByPatient_notFound_throws() {
        Mockito.when(patientRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByPatient(1L, 5L));
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR
    // ---------------------------------------------------------
    @Test
    void getByDoctor_doctor_checksOwnership() {
        Doctor d = new Doctor();
        d.setId(5L);

        Appointment a = new Appointment();
        AppointmentDto dto = new AppointmentDto();

        Mockito.when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 5L);

        Mockito.when(appointmentRepository.findByDoctorId(5L)).thenReturn(List.of(a));
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        List<AppointmentDto> result = service.getByDoctor(1L, 5L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getByDoctor_admin_allowed() {
        Doctor d = new Doctor();
        d.setId(5L);

        Mockito.when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(false);
        Mockito.doNothing().when(adminService).checkAdmin(1L);

        Mockito.when(appointmentRepository.findByDoctorId(5L)).thenReturn(List.of());

        List<AppointmentDto> result = service.getByDoctor(1L, 5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByDoctor_notFound_throws() {
        Mockito.when(doctorRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByDoctor(1L, 5L));
    }

    // ---------------------------------------------------------
    // ACCEPT
    // ---------------------------------------------------------
    @Test
    void accept_doctor_accepts() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.BOOKED);

        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 10L);

        Mockito.when(appointmentRepository.save(a)).thenReturn(a);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.accept(1L, 10L);

        assertSame(dto, result);
        assertEquals(AppointmentStatus.ACCEPTED, a.getStatus());
    }

    @Test
    void accept_cancelled_throws() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.CANCELLED);

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.accept(1L, 10L));
    }

    @Test
    void accept_notFound_throws() {
        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.accept(1L, 10L));
    }

    // ---------------------------------------------------------
    // REJECT
    // ---------------------------------------------------------
    @Test
    void reject_setsRejected() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.BOOKED);

        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 10L);

        Mockito.when(appointmentRepository.save(a)).thenReturn(a);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.reject(1L, 10L);

        assertSame(dto, result);
        assertEquals(AppointmentStatus.REJECTED, a.getStatus());
    }

    // ---------------------------------------------------------
    // COMPLETE
    // ---------------------------------------------------------
    @Test
    void complete_setsCompleted() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.BOOKED);

        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 10L);

        Mockito.when(appointmentRepository.save(a)).thenReturn(a);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.complete(1L, 10L);

        assertSame(dto, result);
        assertEquals(AppointmentStatus.COMPLETED, a.getStatus());
    }

    // ---------------------------------------------------------
    // ADD NOTES
    // ---------------------------------------------------------
    @Test
    void addNotes_setsNotes() {
        Appointment a = new Appointment();
        a.setStatus(AppointmentStatus.BOOKED);

        AppointmentDto dto = new AppointmentDto();

        Mockito.when(appointmentRepository.findById(10L)).thenReturn(Optional.of(a));
        Mockito.when(adminService.isDoctor(1L)).thenReturn(true);
        Mockito.doNothing().when(adminService).checkDoctorOwnsAppointment(1L, 10L);

        Mockito.when(appointmentRepository.save(a)).thenReturn(a);
        Mockito.when(mapper.toDTO(a)).thenReturn(dto);

        AppointmentDto result = service.addNotes(1L, 10L, "test notes");

        assertSame(dto, result);
        assertEquals("test notes", a.getNotes());
    }
}