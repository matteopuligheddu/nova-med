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

        // QUI era l’errore: "appointment" → "a"
        dto.setServiceTypeName(a.getServiceType().getName());

        // Nome completo del paziente
        dto.setPatientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName());

        // Nome completo del medico
        dto.setDoctorName(a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName());

        // Note
        dto.setNotes(a.getNotes());

        return dto;
    }
}