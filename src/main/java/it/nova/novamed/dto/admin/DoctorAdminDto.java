package it.nova.novamed.dto.admin;


import lombok.Data;

@Data
public class DoctorAdminDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;

    private UserAdminDto user;
}

