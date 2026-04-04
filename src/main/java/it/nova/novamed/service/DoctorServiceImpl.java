package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.mapper.DoctorMapper;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final AdminServiceImpl adminService;

    @Override
    public Long getDoctorIdByUserId(Long userId) {
        return doctorRepository.findByUser_Id(userId)
                .map(Doctor::getId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for user " + userId));
    }

    @Override
    public DoctorDto getMyProfile(Long userId) {
        adminService.checkDoctor(userId);

        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return doctorMapper.toDTO(doctor);
    }

    @Override
    public DoctorDto updateMyProfile(Long userId, UpdateDoctorRequest request) {
        adminService.checkDoctor(userId);

        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());

        return doctorMapper.toDTO(doctorRepository.save(doctor));
    }

    @Override
    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(doctorMapper::toDTO)
                .toList();
    }
}