package it.nova.novamed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.dto.patient.UpdatePatientRequest;
import it.nova.novamed.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static it.nova.novamed.util.SessionUtils.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Validated
public class PatientController {

    private final PatientService patientService;

    // ---------------------------------------------------------
    // GET MY PROFILE (OWNER)
    // ---------------------------------------------------------
    @GetMapping("/me")
    public PatientDto getMyProfile(HttpServletRequest request) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return patientService.getMyProfile(userId);
    }

    // ---------------------------------------------------------
    // UPDATE MY PROFILE (OWNER ONLY)
    // ---------------------------------------------------------
    @Operation(summary = "Aggiorna un paziente esistente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paziente aggiornato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Paziente non trovato")
    })
    @PutMapping("/me")
    public PatientDto updateMyProfile(
            HttpServletRequest request,
            @RequestBody UpdatePatientRequest body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return patientService.updateMyProfile(userId, body);
    }
}