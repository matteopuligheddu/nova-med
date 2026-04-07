package it.nova.novamed.mapper;

import it.nova.novamed.dto.doctor.CreateDoctorRequest;
import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.service.ServiceTypeDto;
import it.nova.novamed.model.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorDto toDTO(Doctor doctor) {
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setSpecialization(doctor.getSpecialization());


        if (doctor.getUser() != null) {
            dto.setUserId(doctor.getUser().getId());
            dto.setEmail(doctor.getUser().getEmail());
            dto.setRole(doctor.getUser().getRole().name());
        }


        if (doctor.getServiceTypes() != null) {
            dto.setServiceTypes(
                    doctor.getServiceTypes().stream()
                            .map(st -> new ServiceTypeDto(
                                    st.getId(),
                                    st.getName(),
                                    st.getDescription(),
                                    st.getPrice(),
                                    st.getDoctor().getId(),
                                    st.getDurationMinutes()
                            ))
                            .toList()
            );
        }


        dto.setAppointmentsCount(
                doctor.getAppointments() != null ? doctor.getAppointments().size() : 0
        );

        return dto;
    }


    public Doctor toEntity(CreateDoctorRequest request) {
        Doctor d = new Doctor();
        d.setFirstName(request.getFirstName());
        d.setLastName(request.getLastName());
        d.setSpecialization(request.getSpecialization());
        return d;
    }
}