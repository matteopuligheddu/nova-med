package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;

import java.util.List;

public interface DoctorService {

    DoctorDto getMyProfile(Long userId);

    DoctorDto updateMyProfile(Long userId, UpdateDoctorRequest request);

    List<DoctorDto> getAllDoctors();

    Long getDoctorIdByUserId(Long userId);
}