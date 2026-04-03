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

        // Admin può vedere tutto
        if (adminService.isAdmin(userId)) return;

        // Il dottore può vedere solo i propri slot
        if (adminService.isDoctor(userId)) {
            Doctor doctor = doctorRepo.findByUser_Id(userId)
                    .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

            if (doctor.getId().equals(doctorId)) return;
        }

        // Il paziente può vedere solo slot liberi (filtrati nel controller)
        if (adminService.isPatient(userId)) return;

        throw new UnauthorizedException("Not allowed to view this doctor's slots");
    }

    // ---------------------------------------------------------
    // GENERATE SLOTS
    // ---------------------------------------------------------
    public List<SlotDto> generateSlots(Long userId, Long doctorId, LocalDate date, Long serviceTypeId) {

        checkAccess(userId, doctorId);

        // 1) Disponibilità del medico
        List<DoctorAvailability> availability = availabilityRepo.findByDoctorId(doctorId);

        DayOfWeek dow = date.getDayOfWeek();

        DoctorAvailability dayAvailability = availability.stream()
                .filter(a -> a.getDayOfWeek().equals(dow))
                .findFirst()
                .orElse(null);

        if (dayAvailability == null) return List.of();

        // 2) Durata servizio
        ServiceType service = serviceRepo.findById(serviceTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found"));

        // Controllo che il serviceType appartenga al medico
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!doctor.getServiceTypes().contains(service)) {
            throw new UnauthorizedException("This service does not belong to the doctor");
        }

        int serviceDuration = service.getDurationMinutes();
        int slotMinutes = dayAvailability.getSlotMinutes();

        // 3) Generazione slot
        List<SlotDto> slots = new ArrayList<>();

        LocalTime t = dayAvailability.getStartTime();

        while (!t.plusMinutes(slotMinutes).isAfter(dayAvailability.getEndTime())) {

            Instant slotStart = date.atTime(t).atZone(ZoneId.systemDefault()).toInstant();
            Instant slotEnd = slotStart.plus(slotMinutes, ChronoUnit.MINUTES);

            // Lo slot deve essere abbastanza lungo per contenere il servizio
            if (t.plusMinutes(serviceDuration).isAfter(dayAvailability.getEndTime())) {
                break;
            }

            boolean taken = appointmentRepo.overlaps(doctorId, slotStart, slotEnd);

            slots.add(new SlotDto(t, !taken));

            t = t.plusMinutes(slotMinutes);
        }

        return slots;
    }
}