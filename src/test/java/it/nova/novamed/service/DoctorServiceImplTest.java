package it.nova.novamed.service;

import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.DoctorMapper;
import it.nova.novamed.model.Doctor;
import it.nova.novamed.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @InjectMocks
    private DoctorServiceImpl service;

    @Mock private DoctorRepository doctorRepository;
    @Mock private DoctorMapper doctorMapper;
    @Mock private AdminServiceImpl adminService;

    // ---------------------------------------------------------
    // GET MY PROFILE
    // ---------------------------------------------------------
    @Test
    void getMyProfile_notDoctor_throws() {
        doThrow(new UnauthorizedException("not doctor"))
                .when(adminService).checkDoctor(5L);

        assertThrows(UnauthorizedException.class, () -> service.getMyProfile(5L));
    }

    @Test
    void getMyProfile_doctorNotFound_throws() {
        doNothing().when(adminService).checkDoctor(5L);
        when(doctorRepository.findByUser_Id(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getMyProfile(5L));
    }

    @Test
    void getMyProfile_ok() {
        doNothing().when(adminService).checkDoctor(5L);

        Doctor d = new Doctor();
        DoctorDto dto = new DoctorDto();

        when(doctorRepository.findByUser_Id(5L)).thenReturn(Optional.of(d));
        when(doctorMapper.toDTO(d)).thenReturn(dto);

        DoctorDto result = service.getMyProfile(5L);

        assertSame(dto, result);
    }

    // ---------------------------------------------------------
    // UPDATE MY PROFILE
    // ---------------------------------------------------------
    @Test
    void updateMyProfile_notDoctor_throws() {
        doThrow(new UnauthorizedException("not doctor"))
                .when(adminService).checkDoctor(5L);

        assertThrows(UnauthorizedException.class,
                () -> service.updateMyProfile(5L, new UpdateDoctorRequest()));
    }

    @Test
    void updateMyProfile_doctorNotFound_throws() {
        doNothing().when(adminService).checkDoctor(5L);
        when(doctorRepository.findByUser_Id(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.updateMyProfile(5L, new UpdateDoctorRequest()));
    }

    @Test
    void updateMyProfile_ok() {
        doNothing().when(adminService).checkDoctor(5L);

        Doctor d = new Doctor();
        when(doctorRepository.findByUser_Id(5L)).thenReturn(Optional.of(d));

        UpdateDoctorRequest req = new UpdateDoctorRequest();
        req.setFirstName("Mario");
        req.setLastName("Rossi");
        req.setSpecialization("Cardiology");

        Doctor saved = new Doctor();
        DoctorDto dto = new DoctorDto();

        when(doctorRepository.save(d)).thenReturn(saved);
        when(doctorMapper.toDTO(saved)).thenReturn(dto);

        DoctorDto result = service.updateMyProfile(5L, req);

        assertSame(dto, result);
        assertEquals("Mario", d.getFirstName());
        assertEquals("Rossi", d.getLastName());
        assertEquals("Cardiology", d.getSpecialization());
    }
}