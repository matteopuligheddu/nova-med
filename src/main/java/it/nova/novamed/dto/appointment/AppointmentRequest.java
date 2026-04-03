package it.nova.novamed.dto.appointment;


import java.time.LocalDate;
import java.time.LocalTime;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private Long serviceTypeId;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;
}