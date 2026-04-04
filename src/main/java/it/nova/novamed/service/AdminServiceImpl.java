package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.CreateDoctorRequest;
import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.exception.ForbiddenException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.DoctorMapper;
import it.nova.novamed.mapper.PatientMapper;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;


    // ---------------------------------------------------------
    // GET ALL DOCTORS
    // ---------------------------------------------------------
    public List<PatientDto> getAllPatients(Long adminUserId) {
        checkAdmin(adminUserId);
        return patientRepository.findAll()
                .stream()
                .map(patientMapper::toDTO)
                .toList();
    }

    // ---------------------------------------------------------
    // GET ALL PATIENTS
    // ---------------------------------------------------------
    public List<DoctorDto> getAllDoctors(Long adminUserId) {
        checkAdmin(adminUserId);
        return doctorRepository.findAll()
                .stream()
                .map(doctorMapper::toDTO)
                .toList();
    }

    // ---------------------------------------------------------
    // GET ALL PATIENTS BY ID
    // ---------------------------------------------------------
    public PatientDto getPatientById(Long adminUserId, Long patientId) {
        checkAdmin(adminUserId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return patientMapper.toDTO(patient);
    }

    // ---------------------------------------------------------
    // GET ALL DOCTORS BY ID
    // ---------------------------------------------------------
    public DoctorDto getDoctorById(Long adminUserId, Long doctorId) {
        checkAdmin(adminUserId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return doctorMapper.toDTO(doctor);
    }


    // ---------------------------------------------------------
    // GENERIC USER CREATION
    // ---------------------------------------------------------
    private User createUser(String email, String password, Role role, boolean mustChangePassword) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setMustChangePassword(mustChangePassword);

        return userRepository.save(user);
    }


    // ---------------------------------------------------------
    // CREATE DOCTOR (ADMIN ONLY)
    // ---------------------------------------------------------
    public Doctor createDoctor(Long adminUserId, CreateDoctorRequest request) {

        checkAdmin(adminUserId);

        User user = createUser(
                request.getEmail(),
                request.getPassword(),
                Role.DOCTOR,
                true
        );

        Doctor doctor = new Doctor();
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setUser(user);

        return doctorRepository.save(doctor);
    }

    // ---------------------------------------------------------
    // CREATE PATIENT (ADMIN ONLY)
    // ---------------------------------------------------------
    public Patient createPatient(Long adminUserId, CreatePatientRequest request) {

        checkAdmin(adminUserId);

        User user = createUser(
                request.getEmail(),
                request.getPassword(),
                Role.PATIENT,
                true
        );

        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setUser(user);

        return patientRepository.save(patient);
    }

    // ---------------------------------------------------------
    // UPDATE PATIENT
    // ---------------------------------------------------------
    public PatientDto updatePatient(Long adminUserId, Long patientId, UpdatePatientRequest request) {
        checkAdmin(adminUserId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());

        return patientMapper.toDTO(patientRepository.save(patient));
    }

    // ---------------------------------------------------------
    // UPDATE DOCTOR
    // ---------------------------------------------------------
    public DoctorDto updateDoctor(Long adminUserId, Long doctorId, UpdateDoctorRequest request) {
        checkAdmin(adminUserId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());


        return doctorMapper.toDTO(doctorRepository.save(doctor));
    }

    // ---------------------------------------------------------
    // DELETE USER (ADMIN ONLY)
    // ---------------------------------------------------------
    public void deleteUser(Long adminUserId, Long userIdToDelete) {

        checkAdmin(adminUserId);

        User user = userRepository.findById(userIdToDelete)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new UnauthorizedException("Cannot delete another admin");
        }

        // Delete doctor + appointments
        if (user.getRole() == Role.DOCTOR) {
            doctorRepository.findByUser_Id(userIdToDelete)
                    .ifPresent(doctorRepository::delete); // cascade REMOVE elimina anche gli appuntamenti
        }

        // Delete patient
        if (user.getRole() == Role.PATIENT) {
            patientRepository.findByUser_Id(userIdToDelete)
                    .ifPresent(patientRepository::delete);
        }

        // Finally delete the user
        userRepository.delete(user);
    }

    // ---------------------------------------------------------
    // CHECK ADMIN
    // ---------------------------------------------------------
    public void checkAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admin can perform this action");
        }
    }

    // ---------------------------------------------------------
// CHECK DOCTOR
// ---------------------------------------------------------
    public void checkDoctor(Long userId) {
        if (userId == null)
            throw new UnauthorizedException("User not logged in");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getRole() != Role.DOCTOR)
            throw new ForbiddenException("Only doctors can perform this action");
    }


    // ---------------------------------------------------------
// CHECK PATIENT
// ---------------------------------------------------------
    public void checkPatient(Long userId) {
        if (userId == null)
            throw new UnauthorizedException("User not logged in");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getRole() != Role.PATIENT)
            throw new ForbiddenException("Only patients can perform this action");
    }

    // ---------------------------------------------------------
    // BOOLEAN HELPERS
    // ---------------------------------------------------------
    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public boolean isDoctor(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole() == Role.DOCTOR)
                .orElse(false);
    }

    public boolean isPatient(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole() == Role.PATIENT)
                .orElse(false);
    }

    // ---------------------------------------------------------
    // OWNERSHIP CHECKS (unchanged)
    // ---------------------------------------------------------
    public void checkDoctorOwnsServiceType(Long doctorId, Long serviceTypeId) {
        ServiceType s = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new RuntimeException("ServiceType not found"));

        if (!s.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("This service does not belong to the doctor");
        }
    }

    public void checkDoctorOwnsAppointment(Long userId, Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Doctor d = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UnauthorizedException("User not logged in"));

        if (!a.getDoctor().getId().equals(d.getId())) {
            throw new UnauthorizedException("This appointment does not belong to the doctor");
        }
    }

    public void checkPatientOwnsAppointment(Long userId, Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Patient p = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UnauthorizedException("User not logged in"));

        if (!a.getPatient().getId().equals(p.getId())) {
            throw new UnauthorizedException("This appointment does not belong to the patient");
        }
    }

}