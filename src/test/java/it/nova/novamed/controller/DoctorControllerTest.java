package it.nova.novamed.controller;

import it.nova.novamed.dto.calendar.SlotDto;
import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.service.AdminService;
import it.nova.novamed.service.DoctorService;
import it.nova.novamed.service.SlotService;
import testutil.TestSessions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SlotService slotService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private AdminService adminService;

    // ---------------------------------------------------------
    // GET MY PROFILE
    // ---------------------------------------------------------
    @Test
    void getMyProfile_doctorLoggedIn_returnsProfile() throws Exception {

        MockHttpSession session = TestSessions.doctor(1L);

        DoctorDto dto = new DoctorDto();
        dto.setId(1L);

        Mockito.when(doctorService.getMyProfile(1L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/doctors/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getMyProfile_notLoggedIn_returns403() throws Exception {
        mockMvc.perform(get("/api/doctors/me"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // UPDATE PROFILE
    // ---------------------------------------------------------
    @Test
    void updateMyProfile_doctorLoggedIn_returnsUpdated() throws Exception {

        MockHttpSession session = TestSessions.doctor(1L);

        DoctorDto dto = new DoctorDto();
        dto.setId(1L);

        Mockito.when(doctorService.updateMyProfile(Mockito.eq(1L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/doctors/me")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"firstName":"Mario","lastName":"Rossi"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateMyProfile_notLoggedIn_returns403() throws Exception {
        mockMvc.perform(put("/api/doctors/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"firstName":"Mario","lastName":"Rossi"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // GET SLOTS
    // ---------------------------------------------------------
    @Test
    void getSlots_patient_seesOnlyAvailableSlots() throws Exception {

        MockHttpSession session = TestSessions.patient(1L);

        SlotDto s1 = new SlotDto(LocalTime.of(9, 0), true);
        SlotDto s2 = new SlotDto(LocalTime.of(9, 30), false);

        Mockito.when(slotService.generateSlots(1L, 5L, LocalDate.parse("2025-01-01"), 10L))
                .thenReturn(List.of(s1, s2));

        Mockito.when(adminService.isPatient(1L)).thenReturn(true);

        mockMvc.perform(get("/api/doctors/5/slots")
                        .session(session)
                        .param("date", "2025-01-01")
                        .param("serviceTypeId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].time").value("09:00:00"))
                .andExpect(jsonPath("$[0].free").value(true));
    }

    @Test
    void getSlots_doctor_seesAllSlots() throws Exception {

        MockHttpSession session = TestSessions.doctor(1L);

        SlotDto s1 = new SlotDto(LocalTime.of(9, 0), true);
        SlotDto s2 = new SlotDto(LocalTime.of(9, 30), false);

        Mockito.when(slotService.generateSlots(1L, 5L, LocalDate.parse("2025-01-01"), 10L))
                .thenReturn(List.of(s1, s2));

        Mockito.when(adminService.isPatient(1L)).thenReturn(false);

        mockMvc.perform(get("/api/doctors/5/slots")
                        .session(session)
                        .param("date", "2025-01-01")
                        .param("serviceTypeId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getSlots_notLoggedIn_returns403() throws Exception {
        mockMvc.perform(get("/api/doctors/5/slots")
                        .param("date", "2025-01-01")
                        .param("serviceTypeId", "10"))
                .andExpect(status().isUnauthorized());
    }
}