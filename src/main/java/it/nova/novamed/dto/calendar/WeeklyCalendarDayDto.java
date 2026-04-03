package it.nova.novamed.dto.calendar;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WeeklyCalendarDayDto {

    private LocalDate date;

    private List<CalendarSlotDto> slots;
}
