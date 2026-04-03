package it.nova.novamed.controller;

import it.nova.novamed.dto.appointment.AppointmentRequest;
import it.nova.novamed.dto.calendar.CalendarSlotDto;
import it.nova.novamed.dto.calendar.MonthlyCalendarDto;
import it.nova.novamed.dto.calendar.WeeklyCalendarDto;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.service.AppointmentCalendarService;
import it.nova.novamed.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import it.nova.novamed.dto.appointment.AppointmentDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static it.nova.novamed.util.SessionUtils.requireSession;
import static it.nova.novamed.util.SessionUtils.requireUserId;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Validated
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentCalendarService appointmentCalendarService;

    // ---------------------------------------------------------
    // GET BY ID (admin/doctor/patient - controllo nel service)
    // ---------------------------------------------------------
    @GetMapping("/{id}")
    public AppointmentDto getById(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.getById(userId, id);
    }

    // ---------------------------------------------------------
    // CREATE (PATIENT ONLY - controllo nel service)
    // ---------------------------------------------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto create(
            HttpServletRequest request,
            @Valid @RequestBody AppointmentRequest body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.create(userId, body);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @PutMapping("/{id}")
    public AppointmentDto update(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.update(userId, id, body);
    }

    // ---------------------------------------------------------
    // CANCEL
    // ---------------------------------------------------------
    @PatchMapping("/{id}/cancel")
    public AppointmentDto cancel(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.cancel(userId, id);
    }

    // ---------------------------------------------------------
    // GET BY PATIENT
    // ---------------------------------------------------------
    @GetMapping("/patient/{patientId}")
    public List<AppointmentDto> getByPatient(
            HttpServletRequest request,
            @PathVariable Long patientId
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.getByPatient(userId, patientId);
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentDto> getByDoctor(
            HttpServletRequest request,
            @PathVariable Long doctorId
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.getByDoctor(userId, doctorId);
    }

    // ---------------------------------------------------------
    // CALENDAR DAY
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}/service/{serviceTypeId}/calendar/day")
    public List<CalendarSlotDto> getDoctorCalendar(
            HttpServletRequest httpRequest,
            @PathVariable Long doctorId,
            @PathVariable Long serviceTypeId,
            @RequestParam LocalDate date
    ) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) throw new UnauthorizedException("User not logged in");

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new UnauthorizedException("User not logged in");

        return appointmentCalendarService.getDoctorCalendar(userId, doctorId, serviceTypeId, date);
    }

    // ---------------------------------------------------------
    // CALENDAR WEEK
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}/service/{serviceTypeId}/calendar/week")
    public WeeklyCalendarDto getDoctorWeeklyCalendar(
            HttpServletRequest httpRequest,
            @PathVariable Long doctorId,
            @PathVariable Long serviceTypeId,
            @RequestParam LocalDate date
    ) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) throw new UnauthorizedException("User not logged in");

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new UnauthorizedException("User not logged in");

        return appointmentCalendarService.getDoctorWeeklyCalendar(userId, doctorId, serviceTypeId, date);
    }

    // ---------------------------------------------------------
    // CALENDAR MONTH
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}/service/{serviceTypeId}/calendar/month")
    public MonthlyCalendarDto getDoctorMonthlyCalendar(
            HttpServletRequest httpRequest,
            @PathVariable Long doctorId,
            @PathVariable Long serviceTypeId,
            @RequestParam YearMonth month
    ) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) throw new UnauthorizedException("User not logged in");

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new UnauthorizedException("User not logged in");

        return appointmentCalendarService.getDoctorMonthlyCalendar(userId, doctorId, serviceTypeId, month);
    }

    // ---------------------------------------------------------
    // ACCEPT
    // ---------------------------------------------------------
    @PutMapping("/{id}/accept")
    public AppointmentDto accept(
            HttpServletRequest httpRequest,
            @PathVariable Long id
    ) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) throw new UnauthorizedException("User not logged in");

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new UnauthorizedException("User not logged in");

        return appointmentService.accept(userId, id);
    }

    // ---------------------------------------------------------
    // REJECT
    // ---------------------------------------------------------
    @PutMapping("/{id}/reject")
    public AppointmentDto reject(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.reject(userId, id);
    }

    // ---------------------------------------------------------
    // COMPLETE
    // ---------------------------------------------------------
    @PutMapping("/{id}/complete")
    public AppointmentDto complete(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return appointmentService.complete(userId, id);
    }

    // ---------------------------------------------------------
    // ADD NOTES
    // ---------------------------------------------------------
    @PutMapping("/{id}/notes")
    public AppointmentDto addNotes(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        String notes = body.get("notes");
        return appointmentService.addNotes(userId, id, notes);
    }
}