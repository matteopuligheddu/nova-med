package it.nova.novamed.mapper;

import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.model.Patient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class PatientMapper {

    public PatientDto toDTO(Patient patient) {
        PatientDto dto = new PatientDto();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setBirthDate(patient.getBirthDate());
        dto.setGender(patient.getGender());
        return dto;
    }

    public Patient toEntity(CreatePatientRequest request) {
        Patient p = new Patient();
        p.setFirstName(request.getFirstName());
        p.setLastName(request.getLastName());
        p.setPhoneNumber(request.getPhoneNumber());
        p.setBirthDate(request.getBirthDate());
        p.setGender(request.getGender());

        return p;
    }

}