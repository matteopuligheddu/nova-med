package it.nova.novamed.security;

import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.Patient;
import it.nova.novamed.model.Role;
import it.nova.novamed.model.User;
import it.nova.novamed.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User patientUser;
    private User doctorUser;

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

        // USER PATIENT
        patientUser = new User();
        patientUser.setEmail("p@test.com");
        patientUser.setPassword("pass");
        patientUser.setRole(Role.PATIENT);
        patientUser.setMustChangePassword(false);
        userRepository.save(patientUser);

        Patient patient = new Patient();
        patient.setUser(patientUser);
        patientRepository.save(patient);

        // *** COLLEGAMENTO BIDIREZIONALE ***
        patientUser.setPatient(patient);
        userRepository.save(patientUser);

        // USER DOCTOR
        doctorUser = new User();
        doctorUser.setEmail("d@test.com");
        doctorUser.setPassword("pass");
        doctorUser.setRole(Role.DOCTOR);
        doctorUser.setMustChangePassword(false);
        userRepository.save(doctorUser);

        Doctor doctor = new Doctor();
        doctor.setUser(doctorUser);
        doctorRepository.save(doctor);

        // *** COLLEGAMENTO BIDIREZIONALE ***
        doctorUser.setDoctor(doctor);
        userRepository.save(doctorUser);
    }

    private MockHttpSession patientSession() {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", patientUser.getId());
        s.setAttribute("role",Role.PATIENT);
        return s;
    }

    private MockHttpSession doctorSession() {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("userId", doctorUser.getId());
        s.setAttribute("role",Role.DOCTOR);
        return s;
    }
    // ---------------------------------------------------------
    // 401 — senza sessione
    // ---------------------------------------------------------
    @Test
    void patientEndpoint_requiresLogin() throws Exception {
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // 403 — ruolo sbagliato
    // ---------------------------------------------------------
    @Test
    void patientEndpoint_doctorForbidden() throws Exception {
        mockMvc.perform(get("/api/patients/me").session(doctorSession()))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // 200 — ruolo corretto
    // ---------------------------------------------------------
    @Test
    void patientEndpoint_patientAllowed() throws Exception {
        mockMvc.perform(get("/api/patients/me").session(patientSession()))
                .andExpect(status().isOk());
    }
}