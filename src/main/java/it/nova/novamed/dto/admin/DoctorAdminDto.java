package it.nova.novamed.dto.admin;


import it.nova.novamed.dto.service.ServiceTypeDto;
import lombok.Data;

import java.util.List;

@Data
public class DoctorAdminDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;

    private UserAdminDto user;
    private List<ServiceTypeDto> serviceTypes;
}

