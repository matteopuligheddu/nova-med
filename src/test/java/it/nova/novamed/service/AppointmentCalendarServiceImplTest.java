package it.nova.novamed.service;

import it.nova.novamed.dto.appointment.AppointmentDto;
import it.nova.novamed.dto.calendar.CalendarSlotDto;
import it.nova.novamed.dto.calendar.MonthlyCalendarDto;
import it.nova.novamed.dto.calendar.WeeklyCalendarDto;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.AppointmentMapper;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.ServiceTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentCalendarServiceImplTest {

    @InjectMocks
    private AppointmentCalendarServiceImpl service;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private AppointmentMapper mapper;

    @Mock
    private AdminServiceImpl adminService;

    @Mock
    private DoctorAvailabilityRepository availabilityRepo;

    // ---------------------------------------------------------
    // ACCESS CONTROL
    // ---------------------------------------------------------
    @Test
    void checkAccess_admin_allowed() {
        when(adminService.isAdmin(1L)).thenReturn(true);

        assertDoesNotThrow(() -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void checkAccess_doctorOwner_allowed() {
        Doctor d = new Doctor();
        d.setId(5L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.of(d));

        assertDoesNotThrow(() -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void checkAccess_doctorNotOwner_throws() {
        Doctor d = new Doctor();
        d.setId(99L);

        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.of(d));

        assertThrows(UnauthorizedException.class, () -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void checkAccess_doctorNotFound_throws() {
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(true);
        when(doctorRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> invokeCheckAccess(1L, 5L));
    }

    @Test
    void checkAccess_unknownUser_throws() {
        when(adminService.isAdmin(1L)).thenReturn(false);
        when(adminService.isDoctor(1L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> invokeCheckAccess(1L, 5L));
    }

    private void invokeCheckAccess(Long userId, Long doctorId) {
        try {
            var m = AppointmentCalendarServiceImpl.class.getDeclaredMethod("checkAccess", Long.class, Long.class);
            m.setAccessible(true);
            m.invoke(service, userId, doctorId);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
        }
    }

    // ---------------------------------------------------------
    // DAILY CALENDAR
    // ---------------------------------------------------------
    @Test
    void daily_noAvailability_returnsEmpty() {
        mockAccessOk();

        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setId(10L);
        s.setDurationMinutes(30);

        d.setServiceTypes(List.of(s));

        when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));
        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, DayOfWeek.MONDAY))
                .thenReturn(Optional.empty());

        List<CalendarSlotDto> result = service.getDoctorCalendar(
                1L, 5L, 10L, LocalDate.of(2025, 1, 6)
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void daily_generatesFreeSlots() {
        mockAccessOk();

        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setId(10L);
        s.setDurationMinutes(30);

        d.setServiceTypes(List.of(s));

        DoctorAvailability av = new DoctorAvailability();
        av.setStartTime(LocalTime.of(9, 0));
        av.setEndTime(LocalTime.of(10, 0));
        av.setSlotMinutes(30);

        when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));
        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(av));

        when(appointmentRepository.findByDoctorIdAndDateBetween(any(), any(), any()))
                .thenReturn(List.of());

        List<CalendarSlotDto> result = service.getDoctorCalendar(
                1L, 5L, 10L, LocalDate.of(2025, 1, 6)
        );

        assertEquals(2, result.size());
        assertFalse(result.get(0).isBooked());
    }

    @Test
    void daily_cancelledAppointment_doesNotBlockSlot() {
        mockAccessOk();

        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setId(10L);
        s.setDurationMinutes(30);

        d.setServiceTypes(List.of(s));

        DoctorAvailability av = new DoctorAvailability();
        av.setStartTime(LocalTime.of(9, 0));
        av.setEndTime(LocalTime.of(10, 0));
        av.setSlotMinutes(30);

        Appointment ap = new Appointment();
        ap.setDate(LocalDate.of(2025, 1, 6).atTime(9, 0)
                .atZone(ZoneId.systemDefault()).toInstant());
        ap.setDateEnd(ap.getDate().plusSeconds(1800));
        ap.setStatus(AppointmentStatus.CANCELLED);

        when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));
        when(availabilityRepo.findByDoctorIdAndDayOfWeek(5L, DayOfWeek.MONDAY))
                .thenReturn(Optional.of(av));

        when(appointmentRepository.findByDoctorIdAndDateBetween(any(), any(), any()))
                .thenReturn(List.of(ap));

        List<CalendarSlotDto> result = service.getDoctorCalendar(
                1L, 5L, 10L, LocalDate.of(2025, 1, 6)
        );

        assertFalse(result.get(0).isBooked());
    }

    // ---------------------------------------------------------
    // WEEKLY
    // ---------------------------------------------------------
    @Test
    void weekly_generatesSevenDays() {
        mockAccessOk();

        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setId(10L);
        s.setDurationMinutes(30);

        d.setServiceTypes(List.of(s));

        when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.empty());

        WeeklyCalendarDto result = service.getDoctorWeeklyCalendar(
                1L, 5L, 10L, LocalDate.of(2025, 1, 8)
        );

        assertEquals(7, result.getDays().size());
        assertEquals(DayOfWeek.MONDAY, result.getWeekStart().getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, result.getWeekEnd().getDayOfWeek());
    }

    // ---------------------------------------------------------
    // MONTHLY
    // ---------------------------------------------------------
    @Test
    void monthly_generatesCorrectNumberOfDays() {
        mockAccessOk();

        Doctor d = new Doctor();
        d.setId(5L);

        ServiceType s = new ServiceType();
        s.setId(10L);
        s.setDurationMinutes(30);

        d.setServiceTypes(List.of(s));

        when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
        when(serviceTypeRepository.findById(10L)).thenReturn(Optional.of(s));

        when(availabilityRepo.findByDoctorIdAndDayOfWeek(any(), any()))
                .thenReturn(Optional.empty());

        MonthlyCalendarDto result = service.getDoctorMonthlyCalendar(
                1L, 5L, 10L, YearMonth.of(2025, 1)
        );

        assertEquals(31, result.getDays().size());
    }

    // ---------------------------------------------------------
    // AVAILABLE SLOTS FOR PATIENT
    // ---------------------------------------------------------
    @Test
    void availableSlots_returnsOnlyFree() {
        CalendarSlotDto s1 = new CalendarSlotDto();
        s1.setBooked(false);
        s1.setStartTime(LocalTime.of(9, 0));

        CalendarSlotDto s2 = new CalendarSlotDto();
        s2.setBooked(true);
        s2.setStartTime(LocalTime.of(9, 30));

        AppointmentCalendarServiceImpl spyService = Mockito.spy(service);

        doReturn(List.of(s1, s2)).when(spyService)
                .getDoctorCalendar(any(), any(), any(), any());

        List<LocalTime> result = spyService.getAvailableSlotsForPatient(
                1L, 5L, 10L, LocalDate.of(2025, 1, 6)
        );

        assertEquals(1, result.size());
        assertEquals(LocalTime.of(9, 0), result.get(0));
    }

    // ---------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------
    private void mockAccessOk() {
        when(adminService.isAdmin(any())).thenReturn(true);
    }
}