package it.nova.novamed.service;

import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.DoctorAvailability;
import it.nova.novamed.model.ServiceType;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentValidationServiceImpl implements AppointmentValidationService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepo;

    // ---------------------------------------------------------
    // VALIDATE CREATION
    // ---------------------------------------------------------
    public void validateCreation(Doctor doctor, ServiceType serviceType, Instant start) {

        // 1) Past date
        if (start.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment date cannot be in the past");
        }

        // 2) Service must belong to doctor
        if (!doctor.getServiceTypes().contains(serviceType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ServiceType does not belong to this doctor");
        }

        // 3) Availability
        LocalDate date = start.atZone(ZoneId.systemDefault()).toLocalDate();
        DayOfWeek dow = date.getDayOfWeek();

        DoctorAvailability availability = availabilityRepo
                .findByDoctorIdAndDayOfWeek(doctor.getId(), dow)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Doctor is not free on this day"
                ));

        LocalTime appointmentTime = start.atZone(ZoneId.systemDefault()).toLocalTime();
        int duration = serviceType.getDurationMinutes();
        LocalTime appointmentEnd = appointmentTime.plusMinutes(duration);

        // 4) Inside availability
        if (appointmentTime.isBefore(availability.getStartTime()) ||
                appointmentEnd.isAfter(availability.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment time is outside doctor's availability");
        }

        // 5) Slot alignment (CORRETTO)
        int minutesFromStart = (int) Duration.between(
                availability.getStartTime(),
                appointmentTime
        ).toMinutes();

        if (minutesFromStart % availability.getSlotMinutes() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment time does not align with doctor's slot size (" +
                            availability.getSlotMinutes() + " minutes)");
        }

        // 6) Overlapping
        Instant end = start.plus(duration, ChronoUnit.MINUTES);

        if (appointmentRepository.overlaps(doctor.getId(), start, end)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Doctor already has an overlapping appointment");
        }
    }

    // ---------------------------------------------------------
    // VALIDATE UPDATE
    // ---------------------------------------------------------
    public void validateUpdate(Long appointmentId, Doctor doctor, ServiceType serviceType, Instant start) {

        // 1) Past date
        if (start.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment date cannot be in the past");
        }

        // 2) Service must belong to doctor
        if (!doctor.getServiceTypes().contains(serviceType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ServiceType does not belong to this doctor");
        }

        // 3) Availability
        LocalDate date = start.atZone(ZoneId.systemDefault()).toLocalDate();
        DayOfWeek dow = date.getDayOfWeek();

        DoctorAvailability availability = availabilityRepo
                .findByDoctorIdAndDayOfWeek(doctor.getId(), dow)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Doctor is not free on this day"
                ));

        LocalTime appointmentTime = start.atZone(ZoneId.systemDefault()).toLocalTime();
        int duration = serviceType.getDurationMinutes();
        LocalTime appointmentEnd = appointmentTime.plusMinutes(duration);

        // 4) Inside availability
        if (appointmentTime.isBefore(availability.getStartTime()) ||
                appointmentEnd.isAfter(availability.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment time is outside doctor's availability");
        }

        // 5) Slot alignment (CORRETTO)
        int minutesFromStart = (int) Duration.between(
                availability.getStartTime(),
                appointmentTime
        ).toMinutes();

        if (minutesFromStart % availability.getSlotMinutes() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointment time does not align with doctor's slot size (" +
                            availability.getSlotMinutes() + " minutes)");
        }

        // 6) Overlapping except itself
        Instant end = start.plus(duration, ChronoUnit.MINUTES);

        if (appointmentRepository.overlapsExceptId(appointmentId, doctor.getId(), start, end)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Doctor already has an overlapping appointment");
        }
    }
}