package it.nova.novamed.mapper;

import it.nova.novamed.dto.service.CreateServiceTypeRequest;
import it.nova.novamed.dto.service.ServiceTypeDto;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.ServiceType;
import it.nova.novamed.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class ServiceTypeMapper {

    public ServiceTypeDto toDTO(ServiceType service) {
        ServiceTypeDto dto = new ServiceTypeDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setDoctorId(service.getDoctor() != null ? service.getDoctor().getId() : null);
        dto.setDurationMinutes(service.getDurationMinutes());
        return dto;
    }

    public ServiceType toEntity(CreateServiceTypeRequest request) {
        ServiceType s = new ServiceType();
        s.setName(request.getName());
        s.setDescription(request.getDescription());
        s.setPrice(request.getPrice());
        s.setDurationMinutes(request.getDurationMinutes());
        // il doctor si setta nel service
        return s;
    }
}