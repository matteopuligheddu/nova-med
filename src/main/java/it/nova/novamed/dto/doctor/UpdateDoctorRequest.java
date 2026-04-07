package it.nova.novamed.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;



@Data
public class UpdateDoctorRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String specialization;

    }
