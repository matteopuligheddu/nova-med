package it.nova.novamed.mapper;

import it.nova.novamed.dto.admin.PatientAdminDto;
import it.nova.novamed.dto.admin.UserAdminDto;
import it.nova.novamed.model.Patient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientAdminMapper {

    public static PatientAdminDto toDTO(Patient patient) {
        if (patient == null) return null;

        PatientAdminDto dto = new PatientAdminDto();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setPhoneNumber(patient.getPhoneNumber());

        if (patient.getUser() != null) {
            UserAdminDto userDTO = new UserAdminDto();
            userDTO.setId(patient.getUser().getId());
            userDTO.setEmail(patient.getUser().getEmail());
            dto.setUser(userDTO);
        }

        return dto;
    }

    public static List<PatientAdminDto> toDTOList(List<Patient> patients) {
        return patients.stream()
                .map(PatientAdminMapper::toDTO)
                .toList();
    }
}