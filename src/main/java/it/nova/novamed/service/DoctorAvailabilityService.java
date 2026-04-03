package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.CreateDoctorAvailabilityRequest;
import it.nova.novamed.dto.doctor.DoctorAvailabilityDto;
import it.nova.novamed.dto.doctor.UpdateDoctorAvailabilityRequest;

import java.util.List;

public interface DoctorAvailabilityService {

    DoctorAvailabilityDto create(Long userId, Long doctorId, CreateDoctorAvailabilityRequest request);

    DoctorAvailabilityDto update(Long userId, Long availabilityId, UpdateDoctorAvailabilityRequest request);

    void delete(Long userId, Long availabilityId);

    List<DoctorAvailabilityDto> getByDoctor(Long userId, Long doctorId);
}