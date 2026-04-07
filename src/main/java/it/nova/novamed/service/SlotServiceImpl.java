package it.nova.novamed.service;


import it.nova.novamed.dto.calendar.SlotDto;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.DoctorAvailability;
import it.nova.novamed.model.ServiceType;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.ServiceTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private final DoctorAvailabilityRepository availabilityRepo;
    private final AppointmentRepository appointmentRepo;
    private final ServiceTypeRepository serviceRepo;
    private final DoctorRepository doctorRepo;
    private final AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // ACCESS CONTROL
    // ---------------------------------------------------------
    private void checkAccess(Long userId, Long doctorId) {


        if (adminService.isAdmin(userId)) return;


        if (adminService.isDoctor(userId)) {
            Doctor doctor = doctorRepo.findByUser_Id(userId)
                    .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

            if (doctor.getId().equals(doctorId)) return;
        }


        if (adminService.isPatient(userId)) return;

        throw new UnauthorizedException("Not allowed to view this doctor's slots");
    }

    // ---------------------------------------------------------
    // GENERATE SLOTS
    // ---------------------------------------------------------
    public List<SlotDto> generateSlots(Long userId, Long doctorId, LocalDate date, Long serviceTypeId) {

        checkAccess(userId, doctorId);


        DoctorAvailability availability = availabilityRepo
                .findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek())
                .orElse(null);

        if (availability == null) return List.of();


        ServiceType service = serviceRepo.findById(serviceTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found"));

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!doctor.getServiceTypes().contains(service)) {
            throw new UnauthorizedException("This service does not belong to the doctor");
        }

        int serviceDuration = service.getDurationMinutes();


        List<SlotDto> slots = new ArrayList<>();

        LocalTime t = availability.getStartTime();
        LocalTime end = availability.getEndTime();

        while (!t.plusMinutes(serviceDuration).isAfter(end)) {

            Instant slotStart = date.atTime(t).atZone(ZoneId.systemDefault()).toInstant();
            Instant slotEnd = slotStart.plus(serviceDuration, ChronoUnit.MINUTES);

            boolean taken = appointmentRepo.overlaps(doctorId, slotStart, slotEnd);

            slots.add(new SlotDto(t, !taken));


            t = t.plusMinutes(serviceDuration);
        }

        return slots;
    }
}