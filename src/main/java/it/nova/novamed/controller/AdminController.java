package it.nova.novamed.controller;

import it.nova.novamed.dto.admin.DoctorAdminDto;
import it.nova.novamed.dto.admin.PatientAdminDto;
import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.dto.doctor.CreateDoctorRequest;
import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.DoctorAdminMapper;
import it.nova.novamed.mapper.PatientAdminMapper;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.Patient;
import it.nova.novamed.model.Role;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.PatientRepository;
import it.nova.novamed.service.AdminService;
import it.nova.novamed.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static it.nova.novamed.util.SessionUtils.requireSession;
import static it.nova.novamed.util.SessionUtils.requireUserId;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorAdminMapper doctorAdminMapper;

    // ---------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------

    private void ensureAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        if (role == null || role != Role.ADMIN) {
            throw new UnauthorizedException("Only admin can access this");
        }
    }


    // ---------------------------------------------------------
// CREATE DOCTOR
// ---------------------------------------------------------
    @PostMapping("/doctors")
    public Doctor createDoctor(
            HttpServletRequest request,
            @RequestBody CreateDoctorRequest body
    ) {
        HttpSession session = requireSession(request);
        ensureAdmin(session);

        Long userId = requireUserId(session);
        return adminService.createDoctor(userId, body);
    }

    // ---------------------------------------------------------
// CREATE PATIENT
// ---------------------------------------------------------
    @PostMapping("/patients")
    public Patient createPatient(
            HttpServletRequest request,
            @RequestBody CreatePatientRequest body
    ) {
        HttpSession session = requireSession(request);
        ensureAdmin(session);

        Long userId = requireUserId(session);
        return adminService.createPatient(userId, body);
    }

    // ---------------------------------------------------------
// GET ALL PATIENTS
// ---------------------------------------------------------
    @GetMapping("/patients")
    public List<PatientAdminDto> getAllPatients() {
        return PatientAdminMapper.toDTOList(patientRepository.findAll());
    }

    // ---------------------------------------------------------
// GET ALL DOCTORS
// ---------------------------------------------------------
    @GetMapping("/doctors")
    public List<DoctorAdminDto> getAllDoctors() {
        return doctorAdminMapper.toDTOList(doctorRepository.findAll());
    }


    // ---------------------------------------------------------
// GET PATIENT BY ID
// ---------------------------------------------------------
    @GetMapping("/patients/{patientId}")
    public PatientAdminDto getPatient(@PathVariable Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return PatientAdminMapper.toDTO(patient);
    }

    // ---------------------------------------------------------
// GET DOCTOR BY ID
// ---------------------------------------------------------
    @GetMapping("/doctors/{doctorId}")
    public DoctorAdminDto getDoctor(@PathVariable Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return doctorAdminMapper.toDTO(doctor);
    }


    // ---------------------------------------------------------
// UPDATE PATIENT
// ---------------------------------------------------------
    @PutMapping("/patients/{id}")
    public PatientDto updatePatient(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody UpdatePatientRequest body
    ) {
        HttpSession session = requireSession(request);
        ensureAdmin(session);

        Long userId = requireUserId(session);
        return adminService.updatePatient(userId, id, body);
    }

    // ---------------------------------------------------------
// UPDATE DOCTOR
// ---------------------------------------------------------
    @PutMapping("/doctors/{id}")
    public DoctorDto updateDoctor(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody UpdateDoctorRequest body
    ) {
        HttpSession session = requireSession(request);
        ensureAdmin(session);

        Long userId = requireUserId(session);
        return adminService.updateDoctor(userId, id, body);
    }

    // ---------------------------------------------------------
// DELETE USER
// ---------------------------------------------------------
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            HttpServletRequest request,
            @PathVariable Long userId
    ) {
        HttpSession session = requireSession(request);
        ensureAdmin(session);

        Long adminUserId = requireUserId(session);
        adminService.deleteUser(adminUserId, userId);
    }

    // ---------------------------------------------------------
// GET ALL APPOINTMENTS
// ---------------------------------------------------------
    @GetMapping("/appointments")
    public List<AppointmentDto> getAllAppointments(HttpServletRequest request) {
        HttpSession session = requireSession(request);
        ensureAdmin(session);

        Long userId = requireUserId(session);
        return appointmentService.getAll(userId);
    }

    // ---------------------------------------------------------
// DELETE APPOINTMENT
// ---------------------------------------------------------
    @DeleteMapping("/appointments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAppointment(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        HttpSession session = requireSession(request);
        ensureAdmin(session);

        Long userId = requireUserId(session);
        appointmentService.delete(userId, id);
    }
}