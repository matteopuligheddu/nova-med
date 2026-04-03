package it.nova.novamed.controller;

import it.nova.novamed.dto.doctor.DoctorAvailabilityDto;
import it.nova.novamed.service.DoctorAvailabilityService;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorAvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false)

class DoctorAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorAvailabilityService availabilityService;

    private MockHttpSession session() {
        return TestSessions.doctor(1L);
    }

    @Test
    void create_returnsDto() throws Exception {
        DoctorAvailabilityDto dto = new DoctorAvailabilityDto();
        dto.setId(10L);

        Mockito.when(availabilityService.create(Mockito.eq(1L), Mockito.eq(5L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/availability/doctor/5")
                        .session(session())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"dayOfWeek":"MONDAY","startTime":"09:00","endTime":"12:00"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void update_returnsDto() throws Exception {
        DoctorAvailabilityDto dto = new DoctorAvailabilityDto();
        dto.setId(10L);

        Mockito.when(availabilityService.update(Mockito.eq(1L), Mockito.eq(10L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/availability/10")
                        .session(session())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"startTime":"10:00","endTime":"13:00"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/availability/10").session(session()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getByDoctor_returnsList() throws Exception {
        Mockito.when(availabilityService.getByDoctor(1L, 5L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/availability/doctor/5").session(session()))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/availability/doctor/5"))
                .andExpect(status().isUnauthorized());
    }
}