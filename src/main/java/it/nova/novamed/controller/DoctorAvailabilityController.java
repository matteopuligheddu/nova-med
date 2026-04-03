package it.nova.novamed.controller;

import it.nova.novamed.dto.doctor.CreateDoctorAvailabilityRequest;
import it.nova.novamed.dto.doctor.DoctorAvailabilityDto;
import it.nova.novamed.dto.doctor.UpdateDoctorAvailabilityRequest;
import it.nova.novamed.service.DoctorAvailabilityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static it.nova.novamed.util.SessionUtils.*;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @PostMapping("/doctor/{doctorId}")
    public DoctorAvailabilityDto create(
            HttpServletRequest request,
            @PathVariable Long doctorId,
            @RequestBody CreateDoctorAvailabilityRequest body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return availabilityService.create(userId, doctorId, body);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @PutMapping("/{availabilityId}")
    public DoctorAvailabilityDto update(
            HttpServletRequest request,
            @PathVariable Long availabilityId,
            @RequestBody UpdateDoctorAvailabilityRequest body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return availabilityService.update(userId, availabilityId, body);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @DeleteMapping("/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            HttpServletRequest request,
            @PathVariable Long availabilityId
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        availabilityService.delete(userId, availabilityId);
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}")
    public List<DoctorAvailabilityDto> getByDoctor(
            HttpServletRequest request,
            @PathVariable Long doctorId
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return availabilityService.getByDoctor(userId, doctorId);
    }
}