package it.nova.novamed.dto.calendar;

import it.nova.novamed.dto.appointment.AppointmentDto;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CalendarSlotDto {

    private LocalTime startTime;
    private LocalTime endTime;

    private boolean booked;          // true se c’è un appuntamento
    private Long appointmentId;      // id dell’appuntamento
    private String patientName;      // nome del paziente

}