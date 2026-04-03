package it.nova.novamed.service;

import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.mapper.PatientMapper;
import it.nova.novamed.model.Patient;
import it.nova.novamed.model.Role;
import it.nova.novamed.model.User;
import it.nova.novamed.repository.PatientRepository;
import it.nova.novamed.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper patientMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminServiceImpl adminService;



    // GET BY ID (OWNER)
    public PatientDto getMyProfile(Long userId) {
        adminService.checkPatient(userId);

        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return patientMapper.toDTO(patient);
    }

    // REGISTER (PUBLIC)
    public PatientDto register(CreatePatientRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PATIENT);
        user.setMustChangePassword(false);


        User savedUser = userRepository.save(user);

        Patient patient = patientMapper.toEntity(request);
        patient.setUser(savedUser);

        return patientMapper.toDTO(patientRepository.save(patient));
    }

    // UPDATE (OWNER ONLY)
    public PatientDto updateMyProfile(Long userId, UpdatePatientRequest request) {
        adminService.checkPatient(userId);

        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));


        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());

        return patientMapper.toDTO(patientRepository.save(patient));
    }


    private boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }
}