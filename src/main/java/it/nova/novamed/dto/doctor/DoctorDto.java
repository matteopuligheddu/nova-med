package it.nova.novamed.dto.doctor;

import it.nova.novamed.dto.service.ServiceTypeDto;
import lombok.Data;

import java.util.List;

@Data
public class DoctorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;


    private Long userId;
    private String email;
    private String role;


    private List<ServiceTypeDto> serviceTypes;


    private int appointmentsCount;
}