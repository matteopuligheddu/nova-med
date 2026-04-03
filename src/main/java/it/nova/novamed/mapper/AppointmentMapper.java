package it.nova.novamed.mapper;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.dto.appointment.AppointmentRequest;
import it.nova.novamed.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentDto toDTO(Appointment a) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(a.getId());
        dto.setDate(a.getDate());
        dto.setDateEnd(a.getDateEnd());
        dto.setStatus(a.getStatus());
        dto.setPatientId(a.getPatient().getId());
        dto.setDoctorId(a.getDoctor().getId());
        dto.setServiceTypeId(a.getServiceType().getId());

        //  Nome completo del paziente
        dto.setPatientName(
                a.getPatient().getFirstName() + " " + a.getPatient().getLastName()
        );

        //  Nome completo del medico
        dto.setDoctorName(
                a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName()
        );

        //  Note mediche
        dto.setNotes(a.getNotes());

        return dto;
    }
}