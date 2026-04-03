package it.nova.novamed.controller;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.dto.calendar.MonthlyCalendarDto;
import it.nova.novamed.dto.calendar.WeeklyCalendarDto;
import it.nova.novamed.exception.GlobalExceptionHandler;
import it.nova.novamed.service.AppointmentCalendarService;
import it.nova.novamed.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private AppointmentCalendarService appointmentCalendarService;

    private static final long USER_ID = 1L;

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @Test
    void getById_returnsAppointment() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(10L);

        Mockito.when(appointmentService.getById(USER_ID, 10L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/appointments/10")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getById_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/appointments/10"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @Test
    void create_returnsCreated() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(99L);

        Mockito.when(appointmentService.create(Mockito.eq(USER_ID), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/appointments")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"doctorId":5,"serviceTypeId":10,"date":"2025-01-01","time":"10:00"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99L));
    }

    @Test
    void create_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"doctorId":5,"serviceTypeId":10,"date":"2025-01-01","time":"10:00"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @Test
    void update_returnsUpdated() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(10L);

        Mockito.when(appointmentService.update(Mockito.eq(USER_ID), Mockito.eq(10L), Mockito.any()))
                .thenReturn(dto);

        mockMvc.perform(put("/api/appointments/10")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"doctorId":5,"serviceTypeId":10,"date":"2025-01-01","time":"11:00"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    // ---------------------------------------------------------
    // CANCEL
    // ---------------------------------------------------------
    @Test
    void cancel_returnsAppointment() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(10L);

        Mockito.when(appointmentService.cancel(USER_ID, 10L))
                .thenReturn(dto);

        mockMvc.perform(patch("/api/appointments/10/cancel")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    // ---------------------------------------------------------
    // GET BY PATIENT
    // ---------------------------------------------------------
    @Test
    void getByPatient_returnsList() throws Exception {

        Mockito.when(appointmentService.getByPatient(USER_ID, 5L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/patient/5")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "ADMIN"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR
    // ---------------------------------------------------------
    @Test
    void getByDoctor_returnsList() throws Exception {

        Mockito.when(appointmentService.getByDoctor(USER_ID, 5L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/doctor/5")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // CALENDAR DAY
    // ---------------------------------------------------------
    @Test
    void getDoctorCalendar_returnsSlots() throws Exception {

        Mockito.when(appointmentCalendarService.getDoctorCalendar(
                USER_ID, 5L, 10L, LocalDate.parse("2025-01-01")
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/appointments/doctor/5/service/10/calendar/day")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR")
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // CALENDAR WEEK
    // ---------------------------------------------------------
    @Test
    void getDoctorWeeklyCalendar_returnsWeek() throws Exception {

        WeeklyCalendarDto dto = new WeeklyCalendarDto();

        Mockito.when(appointmentCalendarService.getDoctorWeeklyCalendar(
                USER_ID, 5L, 10L, LocalDate.parse("2025-01-01")
        )).thenReturn(dto);

        mockMvc.perform(get("/api/appointments/doctor/5/service/10/calendar/week")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR")
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // CALENDAR MONTH
    // ---------------------------------------------------------
    @Test
    void getDoctorMonthlyCalendar_returnsMonth() throws Exception {

        MonthlyCalendarDto dto = new MonthlyCalendarDto();

        Mockito.when(appointmentCalendarService.getDoctorMonthlyCalendar(
                USER_ID, 5L, 10L, YearMonth.of(2025, 1)
        )).thenReturn(dto);

        mockMvc.perform(get("/api/appointments/doctor/5/service/10/calendar/month")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR")
                        .param("month", "2025-01"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // ACCEPT / REJECT / COMPLETE / NOTES
    // ---------------------------------------------------------
    @Test
    void accept_returnsAppointment() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(10L);

        Mockito.when(appointmentService.accept(USER_ID, 10L))
                .thenReturn(dto);

        mockMvc.perform(put("/api/appointments/10/accept")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void reject_returnsAppointment() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(10L);

        Mockito.when(appointmentService.reject(USER_ID, 10L))
                .thenReturn(dto);

        mockMvc.perform(put("/api/appointments/10/reject")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void complete_returnsAppointment() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(10L);

        Mockito.when(appointmentService.complete(USER_ID, 10L))
                .thenReturn(dto);

        mockMvc.perform(put("/api/appointments/10/complete")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void addNotes_returnsAppointment() throws Exception {

        AppointmentDto dto = new AppointmentDto();
        dto.setId(10L);

        Mockito.when(appointmentService.addNotes(USER_ID, 10L, "note test"))
                .thenReturn(dto);

        mockMvc.perform(put("/api/appointments/10/notes")
                        .sessionAttr("userId", USER_ID)
                        .sessionAttr("role", "DOCTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"notes":"note test"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }
}