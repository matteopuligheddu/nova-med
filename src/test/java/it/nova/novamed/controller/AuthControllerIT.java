package it.nova.novamed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nova.novamed.dto.auth.LoginRequest;
import it.nova.novamed.dto.auth.ChangePasswordRequest;
import it.nova.novamed.model.Role;
import it.nova.novamed.model.User;
import it.nova.novamed.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)

class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorAvailabilityRepository doctorAvailabilityRepository;
    @Autowired private ServiceTypeRepository serviceTypeRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private AdminRepository adminRepository;


    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        doctorAvailabilityRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.PATIENT);
        testUser.setMustChangePassword(false);

        userRepository.save(testUser);
    }

    @Test
    void login_createsSession() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(request().sessionAttribute("userId", testUser.getId()));
    }

    @Test
    void me_withoutSession_unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withSession_returnsUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .sessionAttr("userId", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"));
    }

    @Test
    void changePassword_requiresLogin() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("password123");
        req.setNewPassword("newpass");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_invalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", testUser.getId());

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());

        // Dopo il logout NON devi riutilizzare la vecchia sessione
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}