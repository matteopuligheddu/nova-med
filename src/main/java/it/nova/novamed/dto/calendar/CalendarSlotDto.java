package it.nova.novamed.dto.calendar;


import lombok.Data;

import java.time.LocalTime;

@Data
public class CalendarSlotDto {

    private LocalTime startTime;
    private LocalTime endTime;

    private boolean booked;
    private Long appointmentId;
    private String patientName;

}