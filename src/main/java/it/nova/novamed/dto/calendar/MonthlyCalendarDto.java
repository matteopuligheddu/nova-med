package it.nova.novamed.dto.calendar;

import lombok.Data;

import java.time.YearMonth;
import java.util.List;

@Data
public class MonthlyCalendarDto {

    private YearMonth month;

    private List<MonthlyCalendarDayDto> days;
}
