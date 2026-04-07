package it.nova.novamed.service;

import it.nova.novamed.dto.auth.ChangePasswordRequest;
import it.nova.novamed.dto.auth.LoginRequest;
import it.nova.novamed.dto.auth.LoginResponse;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.model.User;
import it.nova.novamed.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientServiceImpl patientService;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");

        }

        return new LoginResponse(
                user.getId(),
                user.getRole(),
                user.getDoctor() != null ? user.getDoctor().getId() : null
        );

    }
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
    public void changePassword(Long userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));


        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Old password is incorrect");
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setMustChangePassword(false);
        userRepository.save(user);
    }
    public PatientDto register(CreatePatientRequest request) {
        return patientService.register(request);
    }

}