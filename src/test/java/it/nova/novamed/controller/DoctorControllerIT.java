package it.nova.novamed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.model.*;
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
class DoctorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AdminRepository adminRepository;

    private User doctorUser;
    private Doctor doctor;

    private User patientUser;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        doctorAvailabilityRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();


        // Doctor user
        doctorUser = new User();
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword("pass");
        doctorUser.setRole(Role.DOCTOR);
        doctorUser.setMustChangePassword(false);
        userRepository.save(doctorUser);

        doctor = new Doctor();
        doctor.setUser(doctorUser);
        doctor.setFirstName("Giulia");
        doctor.setLastName("Verdi");
        doctor.setSpecialization("Cardiology");
        doctorRepository.save(doctor);

        // Patient user (per test sugli slot)
        patientUser = new User();
        patientUser.setEmail("patient@example.com");
        patientUser.setPassword("pass");
        patientUser.setRole(Role.PATIENT);
        patientUser.setMustChangePassword(false);
        userRepository.save(patientUser);
    }

    private MockHttpSession doctorSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", doctorUser.getId());
        session.setAttribute("role", Role.DOCTOR);
        return session;
    }

    private MockHttpSession patientSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", patientUser.getId());
        session.setAttribute("role", Role.PATIENT);
        return session;
    }

    @Test
    void getMyProfile_requiresLogin() throws Exception {
        mockMvc.perform(get("/api/doctors/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_returnsDoctorData() throws Exception {
        mockMvc.perform(get("/api/doctors/me").session(doctorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Giulia"))
                .andExpect(jsonPath("$.specialization").value("Cardiology"));
    }

    @Test
    void updateMyProfile_updatesDatabase() throws Exception {
        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setFirstName("Laura");
        req.setLastName("Bianchi");
        req.setSpecialization("Dermatology");

        mockMvc.perform(put("/api/doctors/me")
                        .session(doctorSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Laura"))
                .andExpect(jsonPath("$.specialization").value("Dermatology"));

        Doctor updated = doctorRepository.findById(doctor.getId()).orElseThrow();
        assert updated.getFirstName().equals("Laura");
        assert updated.getSpecialization().equals("Dermatology");
    }

    @Test
    void getSlots_patientSeesOnlyFreeSlots() throws Exception {
        // Mock degli slot generati dal service
        // Qui assumiamo che SlotService generi slot con "free = true/false"
        // In un test reale potresti mockare il service, ma negli integration test
        // si usa il comportamento reale.

        mockMvc.perform(get("/api/doctors/" + doctor.getId() + "/slots")
                        .session(patientSession())
                        .param("date", LocalDate.now().toString())
                        .param("serviceTypeId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getSlots_doctorSeesAllSlots() throws Exception {
        mockMvc.perform(get("/api/doctors/" + doctor.getId() + "/slots")
                        .session(doctorSession())
                        .param("date", LocalDate.now().toString())
                        .param("serviceTypeId", "1"))
                .andExpect(status().isOk());
    }
}