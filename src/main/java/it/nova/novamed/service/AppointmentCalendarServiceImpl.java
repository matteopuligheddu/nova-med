package it.nova.novamed.service;

import it.nova.novamed.dto.calendar.*;
import it.nova.novamed.exception.ResourceNotFoundException;
import it.nova.novamed.exception.UnauthorizedException;
import it.nova.novamed.mapper.AppointmentMapper;
import it.nova.novamed.model.*;
import it.nova.novamed.repository.AppointmentRepository;
import it.nova.novamed.repository.DoctorAvailabilityRepository;
import it.nova.novamed.repository.DoctorRepository;
import it.nova.novamed.repository.ServiceTypeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentCalendarServiceImpl implements AppointmentCalendarService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final AppointmentMapper mapper;
    private final AdminServiceImpl adminService;
    private final DoctorAvailabilityRepository availabilityRepo;

    // ---------------------------------------------------------
    // ACCESS CONTROL
    // ---------------------------------------------------------
    private void checkAccess(Long userId, Long doctorId) {

        // Admin può vedere tutto
        if (adminService.isAdmin(userId)) return;

        // Il dottore può vedere solo il proprio calendario
        if (adminService.isDoctor(userId)) {
            Doctor doctor = doctorRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new UnauthorizedException("Doctor not found"));

            if (doctor.getId().equals(doctorId)) return;
        }

        // Il paziente può vedere il calendario del medico (solo per prenotare)
        if (adminService.isPatient(userId)) {
            return;
        }

        throw new UnauthorizedException("Not allowed to view this calendar");
    }

    // ---------------------------------------------------------
    // DAILY CALENDAR
    // ---------------------------------------------------------
    public List<CalendarSlotDto> getDoctorCalendar(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            LocalDate date
    ) {
        checkAccess(userId, doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found"));

        if (!doctor.getServiceTypes().contains(serviceType)) {
            throw new IllegalArgumentException("This service does not belong to the doctor");
        }

        int serviceDuration = serviceType.getDurationMinutes();

        DayOfWeek dow = date.getDayOfWeek();

        DoctorAvailability availability = availabilityRepo
                .findByDoctorIdAndDayOfWeek(doctorId, dow)
                .orElse(null);

        if (availability == null) return List.of();

        int slotMinutes = availability.getSlotMinutes();
        LocalTime start = availability.getStartTime();
        LocalTime end = availability.getEndTime();

        if (slotMinutes <= 0) {
            throw new IllegalStateException("Invalid slotMinutes: " + slotMinutes);
        }

        if (start == null || end == null) {
            throw new IllegalStateException("Availability times cannot be null");
        }

        if (!start.isBefore(end)) {
            throw new IllegalStateException("Invalid time range: " + start + " - " + end);
        }

        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndDateBetween(doctorId, startOfDay, endOfDay);

        List<CalendarSlotDto> result = new ArrayList<>();

        LocalTime t = start;

        while (!t.plusMinutes(slotMinutes).isAfter(end)) {
            LocalTime slotStartTime = t;
            LocalTime slotEndTime = t.plusMinutes(slotMinutes);

            Instant slotStart = date.atTime(slotStartTime).atZone(ZoneId.systemDefault()).toInstant();
            Instant slotEnd = date.atTime(slotEndTime).atZone(ZoneId.systemDefault()).toInstant();

            boolean isOccupied = appointments.stream()
                    .anyMatch(a ->
                            a.getStatus() != AppointmentStatus.CANCELLED &&
                                    slotStart.isBefore(a.getDateEnd()) &&
                                    slotEnd.isAfter(a.getDate())
                    );

            CalendarSlotDto dto = new CalendarSlotDto();
            dto.setStartTime(slotStartTime);
            dto.setEndTime(slotEndTime);

            if (isOccupied) {
                dto.setBooked(true);

                Appointment ap = appointments.stream()
                        .filter(a ->
                                a.getStatus() != AppointmentStatus.CANCELLED &&
                                        slotStart.isBefore(a.getDateEnd()) &&
                                        slotEnd.isAfter(a.getDate()))
                        .findFirst()
                        .orElse(null);

                if (ap != null) {
                    dto.setAppointmentId(ap.getId());
                    dto.setPatientName(ap.getPatient().getFullName());
                }
            } else {
                dto.setBooked(false);
            }

            result.add(dto);
            t = t.plusMinutes(slotMinutes);
        }

        return result;
    }
    // ---------------------------------------------------------
    // WEEKLY CALENDAR
    // ---------------------------------------------------------
    public WeeklyCalendarDto getDoctorWeeklyCalendar(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            LocalDate anyDateInWeek
    ) {
        checkAccess(userId, doctorId);

        LocalDate weekStart = anyDateInWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        WeeklyCalendarDto weekly = new WeeklyCalendarDto();
        weekly.setWeekStart(weekStart);
        weekly.setWeekEnd(weekEnd);

        List<WeeklyCalendarDayDto> days = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);

            WeeklyCalendarDayDto dayDto = new WeeklyCalendarDayDto();
            dayDto.setDate(date);
            dayDto.setSlots(getDoctorCalendar(userId, doctorId, serviceTypeId, date));

            days.add(dayDto);
        }

        weekly.setDays(days);
        return weekly;
    }

    // ---------------------------------------------------------
    // MONTHLY CALENDAR
    // ---------------------------------------------------------
    public MonthlyCalendarDto getDoctorMonthlyCalendar(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            YearMonth month
    ) {
        checkAccess(userId, doctorId);

        LocalDate firstDay = month.atDay(1);
        LocalDate lastDay = month.atEndOfMonth();

        MonthlyCalendarDto monthly = new MonthlyCalendarDto();
        monthly.setMonth(month);

        List<MonthlyCalendarDayDto> days = new ArrayList<>();

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {

            MonthlyCalendarDayDto dayDto = new MonthlyCalendarDayDto();
            dayDto.setDate(date);
            dayDto.setSlots(getDoctorCalendar(userId, doctorId, serviceTypeId, date));

            days.add(dayDto);
        }

        monthly.setDays(days);
        return monthly;
    }

    // ---------------------------------------------------------
    // AVAILABLE SLOTS FOR PATIENT
    // ---------------------------------------------------------
    public List<LocalTime> getAvailableSlotsForPatient(
            Long userId,
            Long doctorId,
            Long serviceTypeId,
            LocalDate date
    ) {
        // Il paziente può vedere solo slot liberi, non il calendario completo
        // Quindi NON deve vedere appuntamenti BOOKED

        List<CalendarSlotDto> slots = getDoctorCalendar(
                userId,
                doctorId,
                serviceTypeId,
                date
        );

        return slots.stream()
                .filter(s -> !s.isBooked())   // slot liberi
                .map(CalendarSlotDto::getStartTime)
                .toList();
    }
}