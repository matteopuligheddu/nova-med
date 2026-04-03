package it.nova.novamed.mapper;

import it.nova.novamed.dto.admin.DoctorAdminDto;
import it.nova.novamed.dto.admin.UserAdminDto;
import it.nova.novamed.model.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoctorAdminMapper {

    public static DoctorAdminDto toDTO(Doctor doctor) {
        if (doctor == null) return null;

        DoctorAdminDto dto = new DoctorAdminDto();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setSpecialization(doctor.getSpecialization());

        if (doctor.getUser() != null) {
            UserAdminDto userDTO = new UserAdminDto();
            userDTO.setId(doctor.getUser().getId());
            userDTO.setEmail(doctor.getUser().getEmail());
            dto.setUser(userDTO);
        }

        return dto;
    }

    public static List<DoctorAdminDto> toDTOList(List<Doctor> doctors) {
        return doctors.stream()
                .map(DoctorAdminMapper::toDTO)
                .toList();
    }
}