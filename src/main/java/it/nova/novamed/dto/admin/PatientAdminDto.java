package it.nova.novamed.dto.admin;

import lombok.Data;

@Data
public class PatientAdminDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private UserAdminDto user;

}
