package it.nova.novamed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.nova.novamed.dto.calendar.SlotDto;
import it.nova.novamed.dto.doctor.DoctorDto;
import it.nova.novamed.dto.doctor.UpdateDoctorRequest;
import it.nova.novamed.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static it.nova.novamed.util.SessionUtils.*;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {

    private final SlotService slotService;
    private final DoctorService doctorService;
    private final AdminService adminService;

    // ---------------------------------------------------------
    // GET MY PROFILE (OWNER)
    // ---------------------------------------------------------
    @GetMapping("/me")
    public DoctorDto getMyProfile(HttpServletRequest request) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return doctorService.getMyProfile(userId);
    }

    // ---------------------------------------------------------
    // UPDATE MY PROFILE (OWNER)
    // ---------------------------------------------------------
    @Operation(summary = "Aggiorna un medico esistente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medico aggiornato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Medico non trovato")
    })
    @PutMapping("/me")
    public DoctorDto updateMyProfile(
            HttpServletRequest request,
            @RequestBody UpdateDoctorRequest req
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return doctorService.updateMyProfile(userId, req);
    }

    // ---------------------------------------------------------
    // GET SLOTS
    // ---------------------------------------------------------
    @GetMapping("/{id}/slots")
    public List<SlotDto> getSlots(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam LocalDate date,
            @RequestParam Long serviceTypeId
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        List<SlotDto> slots = slotService.generateSlots(
                userId,
                id,
                date,
                serviceTypeId
        );

        // Paziente → vede solo slot liberi
        if (adminService.isPatient(userId)) {
            return slots.stream()
                    .filter(SlotDto::free)
                    .toList();
        }

        // Admin e Doctor → vedono tutto
        return slots;
    }

    // LISTA MEDICI (visibile a tutti gli utenti loggati)
    @GetMapping
    public List<DoctorDto> getAllDoctors(HttpServletRequest request) {
        HttpSession session = requireSession(request);
        requireUserId(session);
        return doctorService.getAllDoctors();
    }


}