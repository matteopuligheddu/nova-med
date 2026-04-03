package it.nova.novamed.controller;

import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.service.PatientService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)

class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Test
    void getMyProfile_loggedIn_returnsProfile() throws Exception {

        MockHttpSession session = TestSessions.patient(1L);

        PatientDto dto = new PatientDto();
        dto.setId(1L);

        Mockito.when(patientService.getMyProfile(1L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/patients/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
    @Test
    void getMyProfile_notLoggedIn_returns403() throws Exception {
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void updateMyProfile_loggedIn_returnsUpdated() throws Exception {

        MockHttpSession session = TestSessions.patient(1L);

        PatientDto dto = new PatientDto();
        dto.setId(1L);

        Mockito.when(patientService.updateMyProfile(Mockito.eq(1L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/patients/me")
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
        mockMvc.perform(put("/api/patients/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"firstName":"Mario","lastName":"Rossi"}
                    """))
                .andExpect(status().isUnauthorized());
    }

}