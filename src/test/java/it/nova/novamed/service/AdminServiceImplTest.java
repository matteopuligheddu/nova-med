package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.dto.doctor.CreateDoctorRequest;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.exception.ForbiddenException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.DoctorMapper;
import it.nova.novamed.mapper.PatientMapper;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @InjectMocks
    private AdminServiceImpl service;

    @Mock private UserRepository userRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private ServiceTypeRepository serviceTypeRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PatientMapper patientMapper;
    @Mock private DoctorMapper doctorMapper;

    // ---------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------
    private User adminUser() {
        User u = new User();
        u.setId(1L);
        u.setRole(Role.ADMIN);
        u.setMustChangePassword(false);
        return u;
    }

    private User doctorUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.DOCTOR);
        u.setMustChangePassword(false);
        return u;
    }

    private User patientUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.PATIENT);
        u.setMustChangePassword(false);
        return u;
    }

    // ---------------------------------------------------------
    // GET ALL PATIENTS
    // ---------------------------------------------------------
    @Test
    void getAllPatients_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        Patient p = new Patient();
        PatientDto dto = new PatientDto();

        when(patientRepository.findAll()).thenReturn(List.of(p));
        when(patientMapper.toDTO(p)).thenReturn(dto);

        List<PatientDto> result = service.getAllPatients(1L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getAllPatients_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.getAllPatients(2L));
    }

    // ---------------------------------------------------------
    // GET ALL DOCTORS
    // ---------------------------------------------------------
    @Test
    void getAllDoctors_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        Doctor d = new Doctor();
        DoctorDto dto = new DoctorDto();

        when(doctorRepository.findAll()).thenReturn(List.of(d));
        when(doctorMapper.toDTO(d)).thenReturn(dto);

        List<DoctorDto> result = service.getAllDoctors(1L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getAllDoctors_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.getAllDoctors(2L));
    }

    // ---------------------------------------------------------
    // GET PATIENT BY ID
    // ---------------------------------------------------------
    @Test
    void getPatientById_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        Patient p = new Patient();
        PatientDto dto = new PatientDto();

        when(patientRepository.findById(10L)).thenReturn(Optional.of(p));
        when(patientMapper.toDTO(p)).thenReturn(dto);

        PatientDto result = service.getPatientById(1L, 10L);

        assertSame(dto, result);
    }

    @Test
    void getPatientById_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));
        when(patientRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getPatientById(1L, 10L));
    }

    @Test
    void getPatientById_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.getPatientById(2L, 10L));
    }

    // ---------------------------------------------------------
    // GET DOCTOR BY ID
    // ---------------------------------------------------------
    @Test
    void getDoctorById_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        Doctor d = new Doctor();
        DoctorDto dto = new DoctorDto();

        when(doctorRepository.findById(10L)).thenReturn(Optional.of(d));
        when(doctorMapper.toDTO(d)).thenReturn(dto);

        DoctorDto result = service.getDoctorById(1L, 10L);

        assertSame(dto, result);
    }

    @Test
    void getDoctorById_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));
        when(doctorRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getDoctorById(1L, 10L));
    }

    @Test
    void getDoctorById_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.getDoctorById(2L, 10L));
    }


    // ---------------------------------------------------------
    // CREATE DOCTOR
    // ---------------------------------------------------------
    @Test
    void createDoctor_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        CreateDoctorRequest req = new CreateDoctorRequest();
        req.setEmail("d@d.com");
        req.setPassword("pwd");
        req.setFirstName("Doc");
        req.setLastName("Tor");
        req.setSpecialization("Cardio");

        when(userRepository.existsByEmail("d@d.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("ENC");

        User savedUser = doctorUser(99L);
        when(userRepository.save(any())).thenReturn(savedUser);

        Doctor savedDoctor = new Doctor();
        when(doctorRepository.save(any())).thenReturn(savedDoctor);

        Doctor result = service.createDoctor(1L, req);

        assertNotNull(result);
    }

    @Test
    void createDoctor_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        CreateDoctorRequest req = new CreateDoctorRequest();

        assertThrows(UnauthorizedException.class, () -> service.createDoctor(2L, req));
    }

    // ---------------------------------------------------------
    // CREATE PATIENT
    // ---------------------------------------------------------
    @Test
    void createPatient_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        CreatePatientRequest req = new CreatePatientRequest();
        req.setEmail("p@p.com");
        req.setPassword("pwd");
        req.setFirstName("Pat");
        req.setLastName("Ient");
        req.setPhoneNumber("123");
        req.setBirthDate(LocalDate.now());
        req.setGender(Gender.MALE); // FIX QUI

        when(userRepository.existsByEmail("p@p.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("ENC");

        User savedUser = patientUser(88L);
        when(userRepository.save(any())).thenReturn(savedUser);

        Patient savedPatient = new Patient();
        when(patientRepository.save(any())).thenReturn(savedPatient);

        Patient result = service.createPatient(1L, req);

        assertNotNull(result);
    }

    @Test
    void createPatient_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        CreatePatientRequest req = new CreatePatientRequest();

        assertThrows(UnauthorizedException.class, () -> service.createPatient(2L, req));
    }

    // ---------------------------------------------------------
    // UPDATE PATIENT
    // ---------------------------------------------------------
    @Test
    void updatePatient_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        Patient p = new Patient();
        when(patientRepository.findById(10L)).thenReturn(Optional.of(p));

        UpdatePatientRequest req = new UpdatePatientRequest();
        req.setFirstName("New");
        req.setLastName("Name");

        Patient saved = new Patient();
        when(patientRepository.save(p)).thenReturn(saved);

        PatientDto dto = new PatientDto();
        when(patientMapper.toDTO(saved)).thenReturn(dto);

        PatientDto result = service.updatePatient(1L, 10L, req);

        assertSame(dto, result);
    }

    @Test
    void updatePatient_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));
        when(patientRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.updatePatient(1L, 10L, new UpdatePatientRequest()));
    }

    @Test
    void updatePatient_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.updatePatient(2L, 10L, new UpdatePatientRequest()));
    }

    // ---------------------------------------------------------
    // UPDATE DOCTOR
    // ---------------------------------------------------------
    @Test
    void updateDoctor_admin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        Doctor d = new Doctor();
        when(doctorRepository.findById(10L)).thenReturn(Optional.of(d));

        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setFirstName("New");
        req.setLastName("Doc");
        req.setSpecialization("Ortho");

        Doctor saved = new Doctor();
        when(doctorRepository.save(d)).thenReturn(saved);

        DoctorDto dto = new DoctorDto();
        when(doctorMapper.toDTO(saved)).thenReturn(dto);

        DoctorDto result = service.updateDoctor(1L, 10L, req);

        assertSame(dto, result);
    }

    @Test
    void updateDoctor_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));
        when(doctorRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.updateDoctor(1L, 10L, new UpdateDoctorRequest()));
    }

    @Test
    void updateDoctor_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.updateDoctor(2L, 10L, new UpdateDoctorRequest()));
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------
    @Test
    void deleteUser_admin_ok_deleteDoctor() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        User doctorUser = doctorUser(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.of(doctorUser));

        Doctor doctor = new Doctor();
        doctor.setId(123L); // <<< AGGIUNTO
        doctor.setUser(doctorUser);

        when(doctorRepository.findByUser_Id(99L)).thenReturn(Optional.of(doctor));

        assertDoesNotThrow(() -> service.deleteUser(1L, 99L));

        verify(doctorRepository).findByUser_Id(99L);
        verify(doctorRepository).delete(doctor);
        verify(userRepository).delete(doctorUser);
    }

    @Test
    void deleteUser_admin_ok_deletePatient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        User patientUser = patientUser(77L);
        when(userRepository.findById(77L)).thenReturn(Optional.of(patientUser));

        Patient patient = new Patient();
        patient.setId(555L); // <<< AGGIUNTO
        patient.setUser(patientUser);

        when(patientRepository.findByUser_Id(77L)).thenReturn(Optional.of(patient));

        assertDoesNotThrow(() -> service.deleteUser(1L, 77L));

        verify(patientRepository).findByUser_Id(77L);
        verify(patientRepository).delete(patient);
        verify(userRepository).delete(patientUser);
    }

    @Test
    void deleteUser_cannotDeleteAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        User otherAdmin = adminUser();
        otherAdmin.setId(50L);

        when(userRepository.findById(50L)).thenReturn(Optional.of(otherAdmin));

        assertThrows(UnauthorizedException.class, () -> service.deleteUser(1L, 50L));
    }

    @Test
    void deleteUser_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.deleteUser(2L, 10L));
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.deleteUser(1L, 10L));
    }

    // ---------------------------------------------------------
    // CHECK ADMIN / DOCTOR / PATIENT
    // ---------------------------------------------------------
    @Test
    void checkAdmin_ok() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        assertDoesNotThrow(() -> service.checkAdmin(1L));
    }

    @Test
    void checkAdmin_notAdmin_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(UnauthorizedException.class, () -> service.checkAdmin(2L));
    }

    @Test
    void checkDoctor_ok() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(doctorUser(5L)));

        assertDoesNotThrow(() -> service.checkDoctor(5L));
    }

    @Test
    void checkDoctor_notDoctor_throws() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertThrows(ForbiddenException.class, () -> service.checkDoctor(2L));
    }

    @Test
    void checkPatient_ok() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(patientUser(7L)));

        assertDoesNotThrow(() -> service.checkPatient(7L));
    }

    @Test
    void checkPatient_notPatient_throws() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(doctorUser(3L)));

        assertThrows(ForbiddenException.class, () -> service.checkPatient(3L));
    }

    // ---------------------------------------------------------
    // BOOLEAN HELPERS
    // ---------------------------------------------------------
    @Test
    void isAdmin_true() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser()));

        assertTrue(service.isAdmin(1L));
    }

    @Test
    void isAdmin_false() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertFalse(service.isAdmin(2L));
    }

    @Test
    void isDoctor_true() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(doctorUser(5L)));

        assertTrue(service.isDoctor(5L));
    }

    @Test
    void isDoctor_false() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(patientUser(2L)));

        assertFalse(service.isDoctor(2L));
    }

    @Test
    void isPatient_true() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(patientUser(7L)));

        assertTrue(service.isPatient(7L));
    }

    @Test
    void isPatient_false() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(doctorUser(3L)));

        assertFalse(service.isPatient(3L));
    }

    // ---------------------------------------------------------
    // OWNERSHIP CHECKS
    // ---------------------------------------------------------
    @Test
    void checkDoctorOwnsServiceType_ok() {
        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setDoctor(d);

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));

        assertDoesNotThrow(() -> service.checkDoctorOwnsServiceType(5L, 10L));
    }

    @Test
    void checkDoctorOwnsServiceType_notOwner_throws() {
        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setDoctor(d);

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));

        assertThrows(UnauthorizedException.class,
                () -> service.checkDoctorOwnsServiceType(99L, 10L));
    }

    @Test
    void checkDoctorOwnsServiceType_notFound_throws() {
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.checkDoctorOwnsServiceType(5L, 10L));
    }

    @Test
    void checkDoctorOwnsAppointment_ok() {
        // Mock del doctor associato all'userId
        Doctor d = new Doctor();
        d.setId(5L);

        when(doctorRepository.findByUser_Id(5L))
                .thenReturn(Optional.of(d));

        // Mock dell'appuntamento
        Appointment a = new Appointment();
        a.setDoctor(d);

        when(appointmentRepository.findById(20L))
                .thenReturn(Optional.of(a));

        assertDoesNotThrow(() -> service.checkDoctorOwnsAppointment(5L, 20L));
    }

    @Test
    void checkDoctorOwnsAppointment_notOwner_throws() {
        Doctor d = new Doctor();
        d.setId(5L);

        Appointment a = new Appointment();
        a.setDoctor(d);

        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(a));

        assertThrows(UnauthorizedException.class,
                () -> service.checkDoctorOwnsAppointment(99L, 20L));
    }

    @Test
    void checkDoctorOwnsAppointment_notFound_throws() {
        when(appointmentRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.checkDoctorOwnsAppointment(5L, 20L));
    }

    @Test
    void checkPatientOwnsAppointment_ok() {
        // Mock del patient associato all'userId
        Patient p = new Patient();
        p.setId(7L);

        when(patientRepository.findByUser_Id(7L))
                .thenReturn(Optional.of(p));

        // Mock dell'appuntamento
        Appointment a = new Appointment();
        a.setPatient(p);

        when(appointmentRepository.findById(30L))
                .thenReturn(Optional.of(a));

        assertDoesNotThrow(() -> service.checkPatientOwnsAppointment(7L, 30L));
    }

    @Test
    void checkPatientOwnsAppointment_notOwner_throws() {
        Patient p = new Patient();
        p.setId(7L);

        Appointment a = new Appointment();
        a.setPatient(p);

        when(appointmentRepository.findById(30L)).thenReturn(Optional.of(a));

        assertThrows(UnauthorizedException.class,
                () -> service.checkPatientOwnsAppointment(99L, 30L));
    }

    @Test
    void checkPatientOwnsAppointment_notFound_throws() {
        when(appointmentRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.checkPatientOwnsAppointment(7L, 30L));
    }
}