package it.nova.novamed.controller;

import it.nova.novamed.dto.calendar.CalendarSlotDto;
import it.nova.novamed.dto.calendar.MonthlyCalendarDto;
import it.nova.novamed.dto.calendar.WeeklyCalendarDto;
import it.nova.novamed.service.AppointmentCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static it.nova.novamed.util.SessionUtils.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Validated
public class CalendarController {

    private final AppointmentCalendarService appointmentCalendarService;

    // ---------------------------------------------------------
    // DAILY
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}/service/{serviceTypeId}/daily")
    public List<CalendarSlotDto> getDoctorDailyCalendar(
            HttpServletRequest request,
            @PathVariable Long doctorId,
            @PathVariable Long serviceTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentCalendarService.getDoctorCalendar(
                userId,
                doctorId,
                serviceTypeId,
                date
        );
    }

    // ---------------------------------------------------------
    // WEEKLY
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}/service/{serviceTypeId}/weekly")
    public WeeklyCalendarDto getDoctorWeeklyCalendar(
            HttpServletRequest request,
            @PathVariable Long doctorId,
            @PathVariable Long serviceTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentCalendarService.getDoctorWeeklyCalendar(
                userId,
                doctorId,
                serviceTypeId,
                date
        );
    }

    // ---------------------------------------------------------
    // MONTHLY
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}/service/{serviceTypeId}/monthly")
    public MonthlyCalendarDto getDoctorMonthlyCalendar(
            HttpServletRequest request,
            @PathVariable Long doctorId,
            @PathVariable Long serviceTypeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentCalendarService.getDoctorMonthlyCalendar(
                userId,
                doctorId,
                serviceTypeId,
                month
        );
    }

    // ---------------------------------------------------------
    // AVAILABLE SLOTS FOR PATIENT
    // ---------------------------------------------------------
    @GetMapping("/patient/available-slots")
    public List<LocalTime> getAvailableSlotsForPatient(
            HttpServletRequest request,
            @RequestParam Long doctorId,
            @RequestParam Long serviceTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentCalendarService.getAvailableSlotsForPatient(
                userId,
                doctorId,
                serviceTypeId,
                date
        );
    }
}