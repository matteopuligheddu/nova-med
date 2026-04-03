package it.nova.novamed.service;

import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;

public interface PatientService {

    PatientDto getMyProfile(Long userId);

    PatientDto register(CreatePatientRequest request);

    PatientDto updateMyProfile(Long userId, UpdatePatientRequest request);
}