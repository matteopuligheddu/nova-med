package it.nova.novamed.dto.calendar;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WeeklyCalendarDto {

    private LocalDate weekStart;
    private LocalDate weekEnd;

    private List<WeeklyCalendarDayDto> days;
}