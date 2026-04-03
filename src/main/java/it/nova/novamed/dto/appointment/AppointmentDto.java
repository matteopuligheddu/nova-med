package it.nova.novamed.dto.appointment;

import it.nova.novamed.model.AppointmentStatus;
import java.time.Instant;
import lombok.Data;

@Data
public class AppointmentDto {

    private Long id;

    private Instant date;
    private Instant dateEnd;

    private AppointmentStatus status;

    private Long patientId;
    private Long doctorId;
    private Long serviceTypeId;
    private String patientName;
    private String doctorName;
    private String notes;
}