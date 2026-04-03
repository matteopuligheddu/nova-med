package it.nova.novamed.service;

import it.nova.novamed.dto.service.CreateServiceTypeRequest;
import it.nova.novamed.dto.service.ServiceTypeDto;
import it.nova.novamed.dto.service.UpdateServiceTypeRequest;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.ServiceTypeMapper;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.model.ServiceType;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.ServiceTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceTypeMapper mapper;
    private final AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // GET ALL (public)
    // ---------------------------------------------------------
    public List<ServiceTypeDto> getAll() {
        return serviceTypeRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR (admin or doctor owner)
    // ---------------------------------------------------------
    public List<ServiceTypeDto> getByDoctor(Long userId, Long doctorId) {

        // Admin può vedere tutto
        if (adminService.isAdmin(userId)) {
            return serviceTypeRepository.findAllByDoctorId(doctorId)
                    .stream()
                    .map(mapper::toDTO)
                    .toList();
        }

        // Il dottore può vedere solo i propri servizi
        if (adminService.isDoctor(userId)) {
            Doctor doctor = doctorRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

            if (!doctor.getId().equals(doctorId)) {
                throw new UnauthorizedException("Not allowed to view this doctor's services");
            }

            return serviceTypeRepository.findAllByDoctorId(doctorId)
                    .stream()
                    .map(mapper::toDTO)
                    .toList();
        }

        throw new UnauthorizedException("Not allowed to view this doctor's services");
    }

    // ---------------------------------------------------------
    // GET BY ID (public)
    // ---------------------------------------------------------
    public ServiceTypeDto getById(Long id) {
        ServiceType serviceType = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found with id " + id));

        return mapper.toDTO(serviceType);
    }

    // ---------------------------------------------------------
    // CREATE (ADMIN ONLY)
    // ---------------------------------------------------------
    public ServiceTypeDto create(Long userId, CreateServiceTypeRequest request) {

        adminService.checkAdmin(userId);

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        ServiceType s = mapper.toEntity(request);
        s.setDoctor(doctor);

        return mapper.toDTO(serviceTypeRepository.save(s));
    }

    // ---------------------------------------------------------
    // UPDATE (ADMIN or DOCTOR OWNER)
    // ---------------------------------------------------------
    public ServiceTypeDto update(Long userId, Long serviceTypeId, UpdateServiceTypeRequest request) {

        ServiceType s = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found"));

        Long doctorId = s.getDoctor().getId();

        // Admin può aggiornare tutto
        if (!adminService.isAdmin(userId)) {

            // Se è un dottore, deve essere il proprietario
            if (adminService.isDoctor(userId)) {
                Doctor doctor = doctorRepository.findByUser_Id(userId)
                        .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

                if (!doctor.getId().equals(doctorId)) {
                    throw new UnauthorizedException("You are not allowed to update this service type");
                }
            } else {
                throw new UnauthorizedException("You are not allowed to update this service type");
            }
        }

        // Impediamo di cambiare il medico del servizio
        if (!doctorId.equals(request.getDoctorId())) {
            throw new UnauthorizedException("Cannot change the doctor of a service type");
        }

        s.setName(request.getName());
        s.setDescription(request.getDescription());
        s.setPrice(request.getPrice());
        s.setDurationMinutes(request.getDurationMinutes());

        return mapper.toDTO(serviceTypeRepository.save(s));
    }

    // ---------------------------------------------------------
    // DELETE (ADMIN ONLY)
    // ---------------------------------------------------------
    public void delete(Long userId, Long serviceTypeId) {

        adminService.checkAdmin(userId);

        if (!serviceTypeRepository.existsById(serviceTypeId)) {
            throw new ResourceNotFoundException("ServiceType not found with id " + serviceTypeId);
        }

        serviceTypeRepository.deleteById(serviceTypeId);
    }
}