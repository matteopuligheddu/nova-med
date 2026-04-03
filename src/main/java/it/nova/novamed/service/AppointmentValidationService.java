package it.nova.novamed.service;

import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.ServiceType;

import java.time.Instant;

public interface AppointmentValidationService {

    void validateCreation(Doctor doctor, ServiceType serviceType, Instant start);

    void validateUpdate(Long appointmentId, Doctor doctor, ServiceType serviceType, Instant start);
}