package it.nova.novamed.dto.doctor;

import it.nova.novamed.dto.service.ServiceTypeDto;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class DoctorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;

    // Dati dell'utente associato
    private Long userId;
    private String email;
    private String role;

    // Servizi offerti
    private List<ServiceTypeDto> serviceTypes;

    // Numero appuntamenti (utile per admin)
    private int appointmentsCount;
}