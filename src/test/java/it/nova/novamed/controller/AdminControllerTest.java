package it.nova.novamed.controller;

import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.Role;
import it.nova.novamed.service.AdminService;
import it.nova.novamed.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AppointmentService appointmentService;

    private static final long ADMIN_ID = 1L;

    // ---------------------------------------------------------
    // GET PATIENTS
    // ---------------------------------------------------------
    @Test
    void getPatients_adminLoggedIn_returnsList() throws Exception {

        Mockito.when(adminService.getAllPatients(ADMIN_ID))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/patients")
                        .sessionAttr("userId", ADMIN_ID)
                        .sessionAttr("role", Role.ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    void getPatients_notLoggedIn_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPatients_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/patients")
                        .sessionAttr("userId", ADMIN_ID)
                        .sessionAttr("role", Role.PATIENT))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // CREATE DOCTOR
    // ---------------------------------------------------------
    @Test
    void createDoctor_adminLoggedIn_returnsDoctor() throws Exception {

        Doctor doctor = new Doctor();
        doctor.setId(10L);

        Mockito.when(adminService.createDoctor(Mockito.eq(ADMIN_ID), Mockito.any()))
                .thenReturn(doctor);

        mockMvc.perform(post("/api/admin/doctors")
                        .sessionAttr("userId", ADMIN_ID)
                        .sessionAttr("role", Role.ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"doc@test.com","password":"1234","firstName":"Mario","lastName":"Rossi"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    // ---------------------------------------------------------
    // DELETE USER
    // ---------------------------------------------------------
    @Test
    void deleteUser_adminLoggedIn_returns204() throws Exception {

        mockMvc.perform(delete("/api/admin/users/5")
                        .sessionAttr("userId", ADMIN_ID)
                        .sessionAttr("role", Role.ADMIN))
                .andExpect(status().isNoContent());

        Mockito.verify(adminService).deleteUser(ADMIN_ID, 5L);
    }

    // ---------------------------------------------------------
    // GET ALL APPOINTMENTS
    // ---------------------------------------------------------
    @Test
    void getAllAppointments_adminLoggedIn_returnsList() throws Exception {

        Mockito.when(appointmentService.getAll(ADMIN_ID))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/appointments")
                        .sessionAttr("userId", ADMIN_ID)
                        .sessionAttr("role", Role.ADMIN))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // DELETE APPOINTMENT
    // ---------------------------------------------------------
    @Test
    void deleteAppointment_adminLoggedIn_returns204() throws Exception {

        mockMvc.perform(delete("/api/admin/appointments/10")
                        .sessionAttr("userId", ADMIN_ID)
                        .sessionAttr("role", Role.ADMIN))
                .andExpect(status().isNoContent());

        Mockito.verify(appointmentService).delete(ADMIN_ID, 10L);
    }
}