package it.nova.novamed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nova.novamed.dto.appointment.AppointmentRequest;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerIT {

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
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User patientUser;
    private Patient patient;

    private User doctorUser;
    private Doctor doctor;

    private ServiceType serviceType;

    // ---------------------------------------------------------
    // CLEAN DATABASE + CREATE BASE DATA
    // ---------------------------------------------------------
    @BeforeEach
    void setup() {

        // ORDER MATTERS — delete children first
        appointmentRepository.deleteAll();
        doctorAvailabilityRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        adminRepository.deleteAll();
        userRepository.deleteAll();

        // PATIENT USER
        patientUser = new User();
        patientUser.setEmail("patient@example.com");
        patientUser.setPassword(passwordEncoder.encode("pass"));
        patientUser.setRole(Role.PATIENT);
        patientUser.setMustChangePassword(false);
        userRepository.save(patientUser);

        patient = new Patient();
        patient.setUser(patientUser);
        patient.setFirstName("Mario");
        patient.setLastName("Rossi");
        patientRepository.save(patient);

        // DOCTOR USER
        doctorUser = new User();
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword(passwordEncoder.encode("pass"));
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

        // SERVICE TYPE
        serviceType = new ServiceType();
        serviceType.setName("Visita");
        serviceType.setDescription("Controllo generale");
        serviceType.setPrice(50);
        serviceType.setDurationMinutes(30);
        serviceType.setDoctor(doctor);
        serviceTypeRepository.save(serviceType);
    }

    // ---------------------------------------------------------
    // SECURITY MOCK SESSIONS
    // ---------------------------------------------------------
    private MockHttpSession patientSession() {
        MockHttpSession session = new MockHttpSession();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        patientUser.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        session.setAttribute("role", Role.PATIENT);

        // 🔥 NECESSARIO PER IL TUO CONTROLLER
        session.setAttribute("userId", patientUser.getId());

        return session;
    }

    private MockHttpSession doctorSession() {
        MockHttpSession session = new MockHttpSession();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        doctorUser.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        session.setAttribute("role", Role.DOCTOR);

        // 🔥 NECESSARIO PER IL TUO CONTROLLER
        session.setAttribute("userId", doctorUser.getId());

        return session;
    }

    // ---------------------------------------------------------
    // CREATE APPOINTMENT
    // ---------------------------------------------------------
    @Test
    void createAppointment_patientOnly() throws Exception {
        AppointmentRequest req = new AppointmentRequest();
        req.setDoctorId(doctor.getId());
        req.setServiceTypeId(serviceType.getId());
        req.setDate(LocalDate.now().plusDays(1));
        req.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .session(patientSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(doctor.getId()))
                .andExpect(jsonPath("$.serviceTypeId").value(serviceType.getId()))
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    void createAppointment_requiresLogin() throws Exception {
        AppointmentRequest req = new AppointmentRequest();
        req.setDoctorId(doctor.getId());
        req.setServiceTypeId(serviceType.getId());
        req.setDate(LocalDate.now().plusDays(1));
        req.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // CANCEL APPOINTMENT
    // ---------------------------------------------------------
    @Test
    void cancelAppointment_patientAllowed() throws Exception {
        Appointment a = new Appointment();
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setServiceType(serviceType);

        a.setDate(LocalDate.now().plusDays(1)
                .atTime(10, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        a.setDateEnd(LocalDate.now().plusDays(1)
                .atTime(10, 30)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        a.setStatus(AppointmentStatus.BOOKED);
        appointmentRepository.save(a);
        // 🔥 DEBUG: controlliamo cosa c’è nella sessione
        MockHttpSession s = patientSession();

        mockMvc.perform(patch("/api/appointments/" + a.getId() + "/cancel")
                        .session(patientSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ---------------------------------------------------------
    // ACCEPT APPOINTMENT
    // ---------------------------------------------------------
    @Test
    void acceptAppointment_doctorAllowed() throws Exception {
        Appointment a = new Appointment();
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setServiceType(serviceType);

        a.setDate(LocalDate.now().plusDays(1)
                .atTime(10, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        a.setDateEnd(LocalDate.now().plusDays(1)
                .atTime(10, 30)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        a.setStatus(AppointmentStatus.BOOKED);
        appointmentRepository.save(a);

        mockMvc.perform(put("/api/appointments/" + a.getId() + "/accept")
                        .session(doctorSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}