package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.CreateDoctorAvailabilityRequest;
import it.nova.novamed.dto.doctor.DoctorAvailabilityDto;
import it.nova.novamed.dto.doctor.UpdateDoctorAvailabilityRequest;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.DoctorAvailabilityMapper;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.DoctorAvailability;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import it.nova.novamed.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepo;
    private final DoctorRepository doctorRepo;
    private final AdminServiceImpl adminService;
    private final DoctorAvailabilityMapper mapper;

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    public DoctorAvailabilityDto create(Long userId, Long doctorId, CreateDoctorAvailabilityRequest request) {

        if (!adminService.isAdmin(userId)) {
            if (adminService.isDoctor(userId)) {

                Doctor doctor = doctorRepo.findByUser_Id(userId)
                        .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

                if (doctor.getId() == null || !doctor.getId().equals(doctorId)) {
                    throw new UnauthorizedException("You cannot create availability for another doctor");
                }

            } else {
                throw new UnauthorizedException("Only admin or doctor can create availability");
            }
        }

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        availabilityRepo.findByDoctorIdAndDayOfWeek(doctorId, request.getDayOfWeek())
                .ifPresent(a -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Availability for this day already exists");
                });

        DoctorAvailability a = new DoctorAvailability();
        a.setDoctor(doctor);
        a.setDayOfWeek(request.getDayOfWeek());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());

        return mapper.toDto(availabilityRepo.save(a));
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    public DoctorAvailabilityDto update(Long userId, Long availabilityId, UpdateDoctorAvailabilityRequest request) {

        DoctorAvailability existing = availabilityRepo.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        Doctor owner = existing.getDoctor();

        if (owner == null || owner.getId() == null) {
            throw new UnauthorizedException("Invalid doctor owner");
        }

        Long doctorId = owner.getId();

        if (!adminService.isAdmin(userId)) {
            if (adminService.isDoctor(userId)) {

                Doctor doctor = doctorRepo.findByUser_Id(userId)
                        .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

                if (!doctor.getId().equals(doctorId)) {
                    throw new UnauthorizedException("You cannot update another doctor's availability");
                }

            } else {
                throw new UnauthorizedException("Only admin or doctor can update availability");
            }
        }

        existing.setDayOfWeek(request.getDayOfWeek());
        existing.setStartTime(request.getStartTime());
        existing.setEndTime(request.getEndTime());


        return mapper.toDto(availabilityRepo.save(existing));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    public void delete(Long userId, Long availabilityId) {

        DoctorAvailability existing = availabilityRepo.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        Doctor owner = existing.getDoctor();


        if (owner == null || owner.getId() == null) {
            throw new UnauthorizedException("Invalid doctor owner");
        }

        Long doctorId = owner.getId();

        if (!adminService.isAdmin(userId)) {
            if (adminService.isDoctor(userId)) {

                Doctor doctor = doctorRepo.findByUser_Id(userId)
                        .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

                if (doctor.getId() == null || !doctor.getId().equals(doctorId)) {
                    throw new UnauthorizedException("You cannot delete another doctor's availability");
                }

            } else {
                throw new UnauthorizedException("Only admin or doctor can delete availability");
            }
        }

        availabilityRepo.delete(existing);
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR
    // ---------------------------------------------------------
    public List<DoctorAvailabilityDto> getByDoctor(Long userId, Long doctorId) {

        if (!adminService.isAdmin(userId)) {
            if (adminService.isDoctor(userId)) {

                Doctor doctor = doctorRepo.findByUser_Id(userId)
                        .orElseThrow(() -> new UnauthorizedException("Doctor not found"));


                if (doctor.getId() == null || !doctor.getId().equals(doctorId)) {
                    throw new UnauthorizedException("You cannot view another doctor's availability");
                }

            } else {
                throw new UnauthorizedException("Only admin or doctor can view availability");
            }
        }

        return availabilityRepo.findByDoctorId(doctorId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
