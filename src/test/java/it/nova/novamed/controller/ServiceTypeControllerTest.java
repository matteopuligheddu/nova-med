package it.nova.novamed.controller;

import it.nova.novamed.dto.service.ServiceTypeDto;
import it.nova.novamed.service.ServiceTypeService;
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

@WebMvcTest(ServiceTypeController.class)
@AutoConfigureMockMvc(addFilters = false)

class ServiceTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceTypeService serviceTypeService;

    private MockHttpSession session() {
        return TestSessions.admin(1L);
    }

    @Test
    void create_returnsCreated() throws Exception {
        ServiceTypeDto dto = new ServiceTypeDto();
        dto.setId(10L);

        Mockito.when(serviceTypeService.create(Mockito.eq(1L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/service-types")
                        .session(session())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Visita Cardiologica","duration":30}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getAll_returnsList() throws Exception {
        Mockito.when(serviceTypeService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/service-types"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returnsDto() throws Exception {
        ServiceTypeDto dto = new ServiceTypeDto();
        dto.setId(5L);

        Mockito.when(serviceTypeService.getById(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/service-types/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    void update_returnsDto() throws Exception {
        ServiceTypeDto dto = new ServiceTypeDto();
        dto.setId(10L);

        Mockito.when(serviceTypeService.update(Mockito.eq(1L), Mockito.eq(10L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/service-types/10")
                        .session(session())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Visita aggiornata","duration":45}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/service-types/10").session(session()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getByDoctor_returnsList() throws Exception {
        Mockito.when(serviceTypeService.getByDoctor(1L, 5L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/service-types/doctor/5").session(session()))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticated_returns403() throws Exception {
        mockMvc.perform(put("/api/service-types/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"Test","duration":30}
                        """))
                .andExpect(status().isUnauthorized());
    }
}