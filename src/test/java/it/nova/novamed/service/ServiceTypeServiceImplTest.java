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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTypeServiceImplTest {

    @InjectMocks
    private ServiceTypeServiceImpl service;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ServiceTypeMapper mapper;

    @Mock
    private AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    @Test
    void getAll_returnsList() {
        ServiceType s = new ServiceType();
        ServiceTypeDto dto = new ServiceTypeDto();

        when(serviceTypeRepository.findAll()).thenReturn(List.of(s));
        when(mapper.toDTO(s)).thenReturn(dto);

        List<ServiceTypeDto> result = service.getAll();

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR
    // ---------------------------------------------------------
    @Test
    void getByDoctor_admin_ok() {
        ServiceType s = new ServiceType();
        ServiceTypeDto dto = new ServiceTypeDto();

        when(adminService.isAdmin(1L)).thenReturn(true);
        when(serviceTypeRepository.findAllByDoctorId(5L)).thenReturn(List.of(s));
        when(mapper.toDTO(s)).thenReturn(dto);

        List<ServiceTypeDto> result = service.getByDoctor(1L, 5L);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getByDoctor_doctorOwner_ok() {
        Doctor doctor = new Doctor();
        doctor.setId(5L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.of(doctor));
        when(serviceTypeRepository.findAllByDoctorId(5L)).thenReturn(List.of());

        List<ServiceTypeDto> result = service.getByDoctor(1L, 5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByDoctor_doctorNotOwner_throws() {
        Doctor doctor = new Doctor();
        doctor.setId(99L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.of(doctor));

        assertThrows(UnauthorizedException.class, () -> service.getByDoctor(1L, 5L));
    }

    @Test
    void getByDoctor_doctorNotFound_throws() {
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> service.getByDoctor(1L, 5L));
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @Test
    void getById_ok() {
        ServiceType s = new ServiceType();
        ServiceTypeDto dto = new ServiceTypeDto();

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));
        when(mapper.toDTO(s)).thenReturn(dto);

        ServiceTypeDto result = service.getById(10L);

        assertSame(dto, result);
    }

    @Test
    void getById_notFound_throws() {
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(10L));
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @Test
    void create_admin_creates() {
        CreateServiceTypeRequest req = new CreateServiceTypeRequest();
        req.setDoctorId(5L);

        Doctor doctor = new Doctor();
        doctor.setId(5L);

        ServiceType s = new ServiceType();
        ServiceTypeDto dto = new ServiceTypeDto();

        when(adminService.isAdmin(1L)).thenReturn(true);
        when(doctorRepository.findById(5L)).thenReturn(Optional.of(doctor));
        when(mapper.toEntity(req)).thenReturn(s);
        when(serviceTypeRepository.save(s)).thenReturn(s);
        when(mapper.toDTO(s)).thenReturn(dto);

        ServiceTypeDto result = service.create(1L, req);

        assertSame(dto, result);
    }

    @Test
    void create_notAdmin_throws() {
        CreateServiceTypeRequest req = new CreateServiceTypeRequest();

        when(adminService.isAdmin(2L)).thenReturn(false);
        when(adminService.isDoctor(2L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.create(2L, req));
    }

    @Test
    void create_doctorNotFound_throws() {
        CreateServiceTypeRequest req = new CreateServiceTypeRequest();
        req.setDoctorId(5L);

        when(adminService.isAdmin(1L)).thenReturn(true);
        when(doctorRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(1L, req));
    }


    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @Test
    void update_admin_updates() {
        UpdateServiceTypeRequest req = new UpdateServiceTypeRequest();
        req.setDoctorId(5L);
        req.setName("New");
        req.setDescription("Desc");
        req.setPrice((int) 100.0);
        req.setDurationMinutes(30);

        Doctor doctor = new Doctor();
        doctor.setId(5L);

        ServiceType s = new ServiceType();
        s.setDoctor(doctor);

        ServiceTypeDto dto = new ServiceTypeDto();

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));
        when(adminService.isAdmin(1L)).thenReturn(true);
        when(serviceTypeRepository.save(s)).thenReturn(s);
        when(mapper.toDTO(s)).thenReturn(dto);

        ServiceTypeDto result = service.update(1L, 10L, req);

        assertSame(dto, result);
        assertEquals("New", s.getName());
    }

    @Test
    void update_doctorOwner_ok() {
        UpdateServiceTypeRequest req = new UpdateServiceTypeRequest();
        req.setDoctorId(5L);

        Doctor doctor = new Doctor();
        doctor.setId(5L);

        ServiceType s = new ServiceType();
        s.setDoctor(doctor);

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.of(doctor));
        when(serviceTypeRepository.save(s)).thenReturn(s);
        when(mapper.toDTO(s)).thenReturn(new ServiceTypeDto());

        assertDoesNotThrow(() -> service.update(1L, 10L, req));
    }

    @Test
    void update_doctorNotOwner_throws() {
        UpdateServiceTypeRequest req = new UpdateServiceTypeRequest();
        req.setDoctorId(5L);

        Doctor owner = new Doctor();
        owner.setId(5L);

        Doctor other = new Doctor();
        other.setId(99L);

        ServiceType s = new ServiceType();
        s.setDoctor(owner);

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.of(other));

        assertThrows(UnauthorizedException.class, () -> service.update(1L, 10L, req));
    }



    @Test
    void update_notFound_throws() {
        UpdateServiceTypeRequest req = new UpdateServiceTypeRequest();

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, 10L, req));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Test
    void delete_admin_ok() {
        when(adminService.isAdmin(1L)).thenReturn(true);

        ServiceType s = new ServiceType();
        Doctor d = new Doctor();
        d.setId(5L);
        s.setDoctor(d);

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));

        assertDoesNotThrow(() -> service.delete(1L, 10L));
        verify(serviceTypeRepository).delete(s);
    }


    @Test
    void delete_notAdmin_throws() {
        // Il service deve trovare il ServiceType PRIMA di controllare i permessi
        ServiceType s = new ServiceType();
        Doctor d = new Doctor();
        d.setId(5L);
        s.setDoctor(d);

        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));

        // Utente non admin e non doctor
        when(adminService.isAdmin(2L)).thenReturn(false);
        when(adminService.isDoctor(2L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.delete(2L, 10L));
    }
}