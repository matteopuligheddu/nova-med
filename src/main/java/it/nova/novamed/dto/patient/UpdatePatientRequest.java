package it.nova.novamed.dto.patient;

import it.nova.novamed.model.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePatientRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phoneNumber;

    private LocalDate birthDate;

    private Gender gender;
}
