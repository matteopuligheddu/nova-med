package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.CreateDoctorRequest;
import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.Patient;

import java.util.List;

public interface AdminService {

    // GET ALL
    List<PatientDto> getAllPatients(Long adminUserId);
    List<DoctorDto> getAllDoctors(Long adminUserId);

    // GET BY ID
    PatientDto getPatientById(Long adminUserId, Long patientId);
    DoctorDto getDoctorById(Long adminUserId, Long doctorId);

    // CREATE
    Doctor createDoctor(Long adminUserId, CreateDoctorRequest request);
    Patient createPatient(Long adminUserId, CreatePatientRequest request);

    // UPDATE
    PatientDto updatePatient(Long adminUserId, Long patientId, UpdatePatientRequest request);
    DoctorDto updateDoctor(Long adminUserId, Long doctorId, UpdateDoctorRequest request);

    // DELETE
    void deleteUser(Long adminUserId, Long userIdToDelete);

    // ROLE CHECKS
    void checkAdmin(Long userId);
    void checkDoctor(Long userId);
    void checkPatient(Long userId);

    boolean isAdmin(Long userId);
    boolean isDoctor(Long userId);
    boolean isPatient(Long userId);

    // OWNERSHIP CHECKS
    void checkDoctorOwnsServiceType(Long doctorId, Long serviceTypeId);
    void checkDoctorOwnsAppointment(Long doctorId, Long appointmentId);
    void checkPatientOwnsAppointment(Long patientId, Long appointmentId);
}