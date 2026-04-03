package it.nova.novamed.dto.patient;
import it.nova.novamed.model.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePatientRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthDate;
    private Gender gender;

    private String email;
    private String password;
}
