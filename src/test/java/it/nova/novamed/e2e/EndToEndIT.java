package it.nova.novamed.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nova.novamed.dto.appointment.AppointmentRequest;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
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
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EndToEndIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    private Doctor doctor;
    private ServiceType serviceType;

    @BeforeEach
    void setup() {
        appointmentRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Create doctor + service type
        User doctorUser = new User();
        doctorUser.setEmail("doc@test.com");
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
        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctor(doctor);
        availability.setDayOfWeek(LocalDate.now().plusDays(1).getDayOfWeek());
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(18, 0));
        availability.setSlotMinutes(30);
        doctorAvailabilityRepository.save(availability);

        serviceType = new ServiceType();
        serviceType.setName("Visita");
        serviceType.setDescription("Controllo generale");
        serviceType.setPrice((int) 50.0);
        serviceType.setDurationMinutes(30);
        serviceType.setDoctor(doctor);
        serviceTypeRepository.save(serviceType);
    }

    @Test
    void fullFlow_patientJourney() throws Exception {

        // ---------------------------------------------------------
        // 1) REGISTRAZIONE
        // ---------------------------------------------------------
        CreatePatientRequest reg = new CreatePatientRequest();
        reg.setEmail("patient@test.com");
        reg.setPassword("pass");
        reg.setFirstName("Mario");
        reg.setLastName("Rossi");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // ---------------------------------------------------------
        // 2) LOGIN
        // ---------------------------------------------------------
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"patient@test.com","password":"pass"}
                                """))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("userId", session.getAttribute("userId")));

        // ---------------------------------------------------------
        // 3) UPDATE PROFILO
        // ---------------------------------------------------------
        UpdatePatientRequest upd = new UpdatePatientRequest();
        upd.setFirstName("MarioUpdated");
        upd.setLastName("RossiUpdated");

        mockMvc.perform(put("/api/patients/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("MarioUpdated"));

        // ---------------------------------------------------------
        // 4) CREA APPUNTAMENTO
        // ---------------------------------------------------------
        AppointmentRequest req = new AppointmentRequest();
        req.setDoctorId(doctor.getId());
        req.setServiceTypeId(serviceType.getId());
        req.setDate(LocalDate.now().plusDays(1));
        req.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"));

        // ---------------------------------------------------------
        // 5) LOGOUT
        // ---------------------------------------------------------
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());
    }
}