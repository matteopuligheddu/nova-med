package it.nova.novamed.dto.doctor;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class CreateDoctorAvailabilityRequest {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private int slotMinutes;
}

