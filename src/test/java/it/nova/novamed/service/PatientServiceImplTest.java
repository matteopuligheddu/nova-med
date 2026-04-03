package it.nova.novamed.service;

import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.PatientMapper;
import it.nova.novamed.model.Gender;
import it.nova.novamed.model.Patient;
import it.nova.novamed.model.Role;
import it.nova.novamed.model.User;
import it.nova.novamed.repository.PatientRepository;
import it.nova.novamed.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @InjectMocks
    private PatientServiceImpl service;

    @Mock private PatientRepository patientRepository;
    @Mock private UserRepository userRepository;
    @Mock private PatientMapper patientMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // GET MY PROFILE
    // ---------------------------------------------------------
    @Test
    void getMyProfile_notPatient_throws() {
        doThrow(new UnauthorizedException("not patient"))
                .when(adminService).checkPatient(5L);

        assertThrows(UnauthorizedException.class, () -> service.getMyProfile(5L));
    }

    @Test
    void getMyProfile_patientNotFound_throws() {
        doNothing().when(adminService).checkPatient(5L);
        when(patientRepository.findByUser_Id(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getMyProfile(5L));
    }

    @Test
    void getMyProfile_ok() {
        doNothing().when(adminService).checkPatient(5L);

        Patient p = new Patient();
        PatientDto dto = new PatientDto();

        when(patientRepository.findByUser_Id(5L)).thenReturn(Optional.of(p));
        when(patientMapper.toDTO(p)).thenReturn(dto);

        PatientDto result = service.getMyProfile(5L);

        assertSame(dto, result);
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------
    @Test
    void register_emailExists_throws() {
        CreatePatientRequest req = new CreatePatientRequest();
        req.setEmail("a@a.com");

        when(userRepository.existsByEmail("a@a.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.register(req));
    }

    @Test
    void register_ok() {
        CreatePatientRequest req = new CreatePatientRequest();
        req.setEmail("a@a.com");
        req.setPassword("pwd");
        req.setFirstName("Mario");
        req.setLastName("Rossi");
        req.setPhoneNumber("123");
        req.setBirthDate(LocalDate.now());
        req.setGender(Gender.MALE);

        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("ENC");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setRole(Role.PATIENT);
        savedUser.setMustChangePassword(false);

        when(userRepository.save(any())).thenReturn(savedUser);

        Patient p = new Patient();
        Patient saved = new Patient();
        PatientDto dto = new PatientDto();

        when(patientMapper.toEntity(req)).thenReturn(p);
        when(patientRepository.save(p)).thenReturn(saved);
        when(patientMapper.toDTO(saved)).thenReturn(dto);

        PatientDto result = service.register(req);

        assertSame(dto, result);
        assertEquals(savedUser, p.getUser());
    }

    // ---------------------------------------------------------
    // UPDATE MY PROFILE
    // ---------------------------------------------------------
    @Test
    void updateMyProfile_notPatient_throws() {
        doThrow(new UnauthorizedException("not patient"))
                .when(adminService).checkPatient(5L);

        assertThrows(UnauthorizedException.class,
                () -> service.updateMyProfile(5L, new UpdatePatientRequest()));
    }

    @Test
    void updateMyProfile_patientNotFound_throws() {
        doNothing().when(adminService).checkPatient(5L);
        when(patientRepository.findByUser_Id(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.updateMyProfile(5L, new UpdatePatientRequest()));
    }

    @Test
    void updateMyProfile_ok() {
        doNothing().when(adminService).checkPatient(5L);

        Patient p = new Patient();
        when(patientRepository.findByUser_Id(5L)).thenReturn(Optional.of(p));

        UpdatePatientRequest req = new UpdatePatientRequest();
        req.setFirstName("Mario");
        req.setLastName("Rossi");
        req.setPhoneNumber("123");
        req.setBirthDate(LocalDate.now());
        req.setGender(Gender.MALE);

        Patient saved = new Patient();
        PatientDto dto = new PatientDto();

        when(patientRepository.save(p)).thenReturn(saved);
        when(patientMapper.toDTO(saved)).thenReturn(dto);

        PatientDto result = service.updateMyProfile(5L, req);

        assertSame(dto, result);
        assertEquals("Mario", p.getFirstName());
        assertEquals("Rossi", p.getLastName());
        assertEquals("123", p.getPhoneNumber());
        assertEquals(Gender.MALE, p.getGender());
    }
}