package it.nova.novamed.dto.calendar;

import it.nova.novamed.dto.appointment.AppointmentDto;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CalendarSlotDto {

    private LocalTime time; // es. 09:00

    private String status; // FREE, BOOKED

    private AppointmentDto appointment; // null se FREE
}