package it.nova.novamed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.model.Gender;
import it.nova.novamed.model.Patient;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper mapper;

    private User patientUser;
    private Patient patient;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Autowired private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired private AdminRepository adminRepository;


    @BeforeEach
    void setup() {


        appointmentRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        doctorAvailabilityRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();


        patientUser = new User();
        patientUser.setEmail("patient@example.com");
        patientUser.setPassword("pass");
        patientUser.setRole(Role.PATIENT);
        patientUser.setMustChangePassword(false);
        userRepository.save(patientUser);

        patient = new Patient();
        patient.setUser(patientUser);
        patient.setFirstName("Mario");
        patient.setLastName("Rossi");
        patient.setPhoneNumber("123456");
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patient.setGender(Gender.MALE);
        patientRepository.save(patient);
    }

    private MockHttpSession patientSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", patientUser.getId());
        session.setAttribute("role", Role.PATIENT);
        return session;
    }

    @Test
    void getMyProfile_requiresLogin() throws Exception {
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_returnsPatientData() throws Exception {
        mockMvc.perform(get("/api/patients/me").session(patientSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.phoneNumber").value("123456"));
    }

    @Test
    void updateMyProfile_updatesDatabase() throws Exception {
        UpdatePatientRequest req = new UpdatePatientRequest();
        req.setFirstName("Giulia");
        req.setLastName("Bianchi");
        req.setPhoneNumber("999999");
        req.setBirthDate(LocalDate.of(2000, 5, 10));
        req.setGender(Gender.FEMALE);

        mockMvc.perform(put("/api/patients/me")
                        .session(patientSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Giulia"))
                .andExpect(jsonPath("$.lastName").value("Bianchi"))
                .andExpect(jsonPath("$.phoneNumber").value("999999"));

        // Verifica che il DB sia stato aggiornato realmente
        Patient updated = patientRepository.findById(patient.getId()).orElseThrow();
        assert updated.getFirstName().equals("Giulia");
        assert updated.getGender() == Gender.FEMALE;
    }
}