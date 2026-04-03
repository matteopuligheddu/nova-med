package it.nova.novamed.service;

import it.nova.novamed.dto.calendar.CalendarSlotDto;
import it.nova.novamed.dto.calendar.WeeklyCalendarDto;
import it.nova.novamed.dto.calendar.MonthlyCalendarDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;


public interface AppointmentCalendarService {
    List<CalendarSlotDto> getDoctorCalendar(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            LocalDate date
    );

    WeeklyCalendarDto getDoctorWeeklyCalendar(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            LocalDate date
    );

    MonthlyCalendarDto getDoctorMonthlyCalendar(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            YearMonth month
    );

    List<LocalTime> getAvailableSlotsForPatient(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            LocalDate date
    );

}
