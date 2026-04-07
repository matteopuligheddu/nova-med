package it.nova.novamed.mapper;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentMapper {

    private final DoctorMapper doctorMapper;
    private final ServiceTypeMapper serviceTypeMapper;

    public AppointmentDto toDTO(Appointment a) {
        AppointmentDto dto = new AppointmentDto();

        dto.setId(a.getId());
        dto.setDate(a.getDate());
        dto.setDateEnd(a.getDateEnd());
        dto.setStatus(a.getStatus());

        dto.setPatientId(a.getPatient().getId());
        dto.setDoctorId(a.getDoctor().getId());
        dto.setServiceTypeId(a.getServiceType().getId());

        dto.setDoctor(doctorMapper.toDTO(a.getDoctor()));
        dto.setServiceType(serviceTypeMapper.toDTO(a.getServiceType()));


        dto.setServiceTypeName(a.getServiceType().getName());


        dto.setPatientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName());


        dto.setDoctorName(a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName());


        dto.setNotes(a.getNotes());

        return dto;
    }
}