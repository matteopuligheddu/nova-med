package it.nova.novamed.controller;

import it.nova.novamed.dto.calendar.MonthlyCalendarDto;
import it.nova.novamed.dto.calendar.WeeklyCalendarDto;
import it.nova.novamed.service.AppointmentCalendarService;
import testutil.TestSessions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
@AutoConfigureMockMvc(addFilters = false)

class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentCalendarService appointmentCalendarService;

    private MockHttpSession session() {
        return TestSessions.patient(1L);
    }

    @Test
    void dailyCalendar_returnsSlots() throws Exception {
        Mockito.when(appointmentCalendarService.getDoctorCalendar(1L, 5L, 10L, LocalDate.parse("2025-01-01")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/calendar/doctor/5/service/10/daily")
                        .session(session())
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk());
    }

    @Test
    void weeklyCalendar_returnsDto() throws Exception {
        WeeklyCalendarDto dto = new WeeklyCalendarDto();

        Mockito.when(appointmentCalendarService.getDoctorWeeklyCalendar(1L, 5L, 10L, LocalDate.parse("2025-01-01")))
                .thenReturn(dto);

        mockMvc.perform(get("/api/calendar/doctor/5/service/10/weekly")
                        .session(session())
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk());
    }

    @Test
    void monthlyCalendar_returnsDto() throws Exception {
        MonthlyCalendarDto dto = new MonthlyCalendarDto();

        Mockito.when(appointmentCalendarService.getDoctorMonthlyCalendar(1L, 5L, 10L, YearMonth.of(2025, 1)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/calendar/doctor/5/service/10/monthly")
                        .session(session())
                        .param("month", "2025-01"))
                .andExpect(status().isOk());
    }

    @Test
    void availableSlots_returnsList() throws Exception {
        Mockito.when(appointmentCalendarService.getAvailableSlotsForPatient(
                1L, 5L, 10L, LocalDate.parse("2025-01-01")
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/calendar/patient/available-slots")
                        .session(session())
                        .param("doctorId", "5")
                        .param("serviceTypeId", "10")
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/calendar/doctor/5/service/10/daily")
                        .param("date", "2025-01-01"))
                .andExpect(status().isUnauthorized());
    }
}