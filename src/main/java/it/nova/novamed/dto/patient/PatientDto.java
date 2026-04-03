package it.nova.novamed.dto.patient;

import it.nova.novamed.model.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientDto {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String phoneNumber;

}
