package it.nova.novamed.service;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.dto.appointment.AppointmentRequest;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.AppointmentMapper;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper mapper;
    private final AppointmentValidationServiceImpl validationService;
    private final AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // GET ALL (ADMIN ONLY)
    // ---------------------------------------------------------
    public List<AppointmentDto> getAll(Long userId) {
        adminService.checkAdmin(userId);

        return appointmentRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BY ID (admin/doctor/patient)
    // ---------------------------------------------------------
    public AppointmentDto getById(Long userId, Long appointmentId) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (adminService.isAdmin(userId)) {
            return mapper.toDTO(a);
        }

        if (adminService.isDoctor(userId)) {
            adminService.checkDoctorOwnsAppointment(userId, appointmentId);
            return mapper.toDTO(a);
        }

        if (adminService.isPatient(userId)) {
            adminService.checkPatientOwnsAppointment(userId, appointmentId);
            return mapper.toDTO(a);
        }

        throw new UnauthorizedException("Not allowed to view this appointment");
    }

    // ---------------------------------------------------------
    // CREATE (PATIENT ONLY)
    // ---------------------------------------------------------
    public AppointmentDto create(Long userId, AppointmentRequest request) {

        adminService.checkPatient(userId);

        Patient p = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Doctor d = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        ServiceType s = d.getServiceTypes().stream()
                .filter(st -> st.getId().equals(request.getServiceTypeId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found for this doctor"));

        Instant start = ZonedDateTime.of(request.getDate(), request.getTime(), ZoneId.systemDefault()).toInstant();

        validationService.validateCreation(d, s, start);

        Instant end = start.plus(s.getDurationMinutes(), ChronoUnit.MINUTES);

        Appointment a = new Appointment();
        a.setDate(start);
        a.setDateEnd(end);
        a.setDoctor(d);
        a.setPatient(p);
        a.setServiceType(s);
        a.setStatus(AppointmentStatus.BOOKED);

        return mapper.toDTO(appointmentRepository.save(a));
    }

    // ---------------------------------------------------------
    // UPDATE (admin/doctor/patient)
    // ---------------------------------------------------------
    public AppointmentDto update(Long userId, Long appointmentId, AppointmentRequest request) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (adminService.isDoctor(userId)) {
            adminService.checkDoctorOwnsAppointment(userId, appointmentId);
        } else if (adminService.isPatient(userId)) {
            adminService.checkPatientOwnsAppointment(userId, appointmentId);
        } else {
            adminService.checkAdmin(userId);
        }

        Doctor d = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        ServiceType s = d.getServiceTypes().stream()
                .filter(st -> st.getId().equals(request.getServiceTypeId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found for this doctor"));

        Instant start = ZonedDateTime.of(request.getDate(), request.getTime(), ZoneId.systemDefault()).toInstant();

        validationService.validateUpdate(appointmentId, d, s, start);

        Instant end = start.plus(s.getDurationMinutes(), ChronoUnit.MINUTES);

        a.setDate(start);
        a.setDateEnd(end);
        a.setDoctor(d);
        a.setServiceType(s);

        return mapper.toDTO(appointmentRepository.save(a));
    }

    // ---------------------------------------------------------
    // CANCEL (admin/doctor/patient)
    // ---------------------------------------------------------
    public AppointmentDto cancel(Long userId, Long appointmentId) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (adminService.isDoctor(userId)) {
            adminService.checkDoctorOwnsAppointment(userId, appointmentId);
        } else if (adminService.isPatient(userId)) {
            adminService.checkPatientOwnsAppointment(userId, appointmentId);
        } else {
            adminService.checkAdmin(userId);
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment already cancelled");
        }

        a.setStatus(AppointmentStatus.CANCELLED);

        return mapper.toDTO(appointmentRepository.save(a));
    }

    // ---------------------------------------------------------
    // DELETE (ADMIN ONLY)
    // ---------------------------------------------------------
    public void delete(Long userId, Long appointmentId) {

        adminService.checkAdmin(userId);

        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Appointment not found");
        }

        appointmentRepository.deleteById(appointmentId);
    }

    // ---------------------------------------------------------
    // GET BY PATIENT (admin/patient)
    // ---------------------------------------------------------
    public List<AppointmentDto> getByPatient(Long userId, Long patientId) {

        // 1. Recupero il paziente loggato
        Patient loggedPatient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // 2. Controllo che stia chiedendo i SUOI appuntamenti
        if (!loggedPatient.getId().equals(patientId)) {
            throw new UnauthorizedException("Not your appointments");
        }

        // 3. Ritorno la lista senza cercare appointment singoli
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR (admin/doctor)
    // ---------------------------------------------------------
    public List<AppointmentDto> getByDoctor(Long doctorId) {
        Doctor d = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // ---------------------------------------------------------
    // ACCEPT (doctor/admin)
    // ---------------------------------------------------------
    public AppointmentDto accept(Long userId, Long appointmentId) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (adminService.isDoctor(userId)) {
            adminService.checkDoctorOwnsAppointment(userId, appointmentId);
        } else {
            adminService.checkAdmin(userId);
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot accept a cancelled appointment");
        }

        a.setStatus(AppointmentStatus.ACCEPTED);

        return mapper.toDTO(appointmentRepository.save(a));
    }

    // ---------------------------------------------------------
    // REJECT (doctor/admin)
    // ---------------------------------------------------------
    public AppointmentDto reject(Long userId, Long appointmentId) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (adminService.isDoctor(userId)) {
            adminService.checkDoctorOwnsAppointment(userId, appointmentId);
        } else {
            adminService.checkAdmin(userId);
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reject a cancelled appointment");
        }

        a.setStatus(AppointmentStatus.REJECTED);

        return mapper.toDTO(appointmentRepository.save(a));
    }

    // ---------------------------------------------------------
    // COMPLETE (doctor/admin)
    // ---------------------------------------------------------
    public AppointmentDto complete(Long userId, Long appointmentId) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (adminService.isDoctor(userId)) {
            adminService.checkDoctorOwnsAppointment(userId, appointmentId);
        } else {
            adminService.checkAdmin(userId);
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled appointment");
        }

        a.setStatus(AppointmentStatus.COMPLETED);

        return mapper.toDTO(appointmentRepository.save(a));
    }

    // ---------------------------------------------------------
    // ADD NOTES (doctor/admin)
    // ---------------------------------------------------------
    public AppointmentDto addNotes(Long userId, Long appointmentId, String notes) {

        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (adminService.isDoctor(userId)) {
            adminService.checkDoctorOwnsAppointment(userId, appointmentId);
        } else {
            adminService.checkAdmin(userId);
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add notes to a cancelled appointment");
        }

        a.setNotes(notes);

        return mapper.toDTO(appointmentRepository.save(a));
    }
}