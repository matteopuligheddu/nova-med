package it.nova.novamed.service;

import it.nova.novamed.dto.auth.ChangePasswordRequest;
import it.nova.novamed.dto.auth.LoginRequest;
import it.nova.novamed.dto.auth.LoginResponse;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.model.User;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    User getUserById(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    PatientDto register(CreatePatientRequest request);
}