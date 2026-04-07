package it.nova.novamed.mapper;

import it.nova.novamed.dto.doctor.DoctorAvailabilityDto;
import it.nova.novamed.model.DoctorAvailability;
import org.springframework.stereotype.Component;

@Component
public class DoctorAvailabilityMapper {

    public DoctorAvailabilityDto toDto(DoctorAvailability a) {
        DoctorAvailabilityDto dto = new DoctorAvailabilityDto();
        dto.setId(a.getId());
        dto.setDoctorId(a.getDoctor().getId());
        dto.setDayOfWeek(a.getDayOfWeek());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        return dto;
    }
}