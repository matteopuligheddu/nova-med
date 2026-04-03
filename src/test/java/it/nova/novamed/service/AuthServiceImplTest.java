package it.nova.novamed.service;

import it.nova.novamed.dto.auth.ChangePasswordRequest;
import it.nova.novamed.dto.auth.LoginRequest;
import it.nova.novamed.dto.auth.LoginResponse;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.model.Role;
import it.nova.novamed.model.User;
import it.nova.novamed.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl service;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PatientServiceImpl patientService;

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    @Test
    void login_emailNotFound_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@a.com");
        req.setPassword("pwd");

        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> service.login(req));
    }

    @Test
    void login_wrongPassword_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@a.com");
        req.setPassword("pwd");

        User u = new User();
        u.setPassword("ENC");
        u.setMustChangePassword(false);

        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pwd", "ENC")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.login(req));
    }

    @Test
    void login_ok() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@a.com");
        req.setPassword("pwd");

        User u = new User();
        u.setId(10L);
        u.setRole(Role.DOCTOR);
        u.setPassword("ENC");
        u.setMustChangePassword(false);

        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pwd", "ENC")).thenReturn(true);

        LoginResponse res = service.login(req);

        assertEquals(10L, res.getUserId());
        assertEquals(Role.DOCTOR, res.getRole());
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------
    @Test
    void getUserById_ok() {
        User u = new User();
        u.setId(5L);
        u.setMustChangePassword(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        User result = service.getUserById(5L);

        assertSame(u, result);
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> service.getUserById(5L));
    }

    // ---------------------------------------------------------
    // CHANGE PASSWORD
    // ---------------------------------------------------------
    @Test
    void changePassword_userNotFound_throws() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> service.changePassword(5L, req));
    }

    @Test
    void changePassword_wrongOldPassword_throws() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        User u = new User();
        u.setPassword("ENC");
        u.setMustChangePassword(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old", "ENC")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.changePassword(5L, req));
    }

    @Test
    void changePassword_ok() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        User u = new User();
        u.setPassword("ENC");
        u.setMustChangePassword(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old", "ENC")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("NEW_ENC");

        when(userRepository.save(u)).thenReturn(u);

        assertDoesNotThrow(() -> service.changePassword(5L, req));
        assertEquals("NEW_ENC", u.getPassword());
        verify(userRepository).save(u);
    }

    // ---------------------------------------------------------
    // REGISTER (delegato)
    // ---------------------------------------------------------
    @Test
    void register_delegatesToPatientService() {
        CreatePatientRequest req = new CreatePatientRequest();
        PatientDto dto = new PatientDto();

        when(patientService.register(req)).thenReturn(dto);

        PatientDto result = service.register(req);

        assertSame(dto, result);
        verify(patientService).register(req);
    }
}