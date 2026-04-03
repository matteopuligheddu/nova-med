package it.nova.novamed.service;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.dto.appointment.AppointmentRequest;

import java.util.List;

public interface AppointmentService {

    AppointmentDto getById(Long userId, Long appointmentId);

    AppointmentDto create(Long userId, AppointmentRequest request);

    AppointmentDto update(Long userId, Long appointmentId, AppointmentRequest request);

    AppointmentDto cancel(Long userId, Long appointmentId);

    List<AppointmentDto> getByPatient(Long userId, Long patientId);

    List<AppointmentDto> getByDoctor(Long userId, Long doctorId);

    AppointmentDto accept(Long userId, Long appointmentId);

    AppointmentDto reject(Long userId, Long appointmentId);

    AppointmentDto complete(Long userId, Long appointmentId);

    AppointmentDto addNotes(Long userId, Long appointmentId, String notes);

    List<AppointmentDto> getAll(Long userId);

    void delete(Long userId, Long appointmentId);

}