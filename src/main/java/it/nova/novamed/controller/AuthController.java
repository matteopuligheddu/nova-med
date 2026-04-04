package it.nova.novamed.controller;

import io.swagger.v3.oas.models.responses.ApiResponse;
import it.nova.novamed.dto.auth.ChangePasswordRequest;
import it.nova.novamed.dto.patient.CreatePatientRequest;
import it.nova.novamed.dto.patient.PatientDto;
import it.nova.novamed.model.User;
import it.nova.novamed.repository.UserRepository;
import it.nova.novamed.service.AuthService;
import it.nova.novamed.dto.auth.LoginRequest;
import it.nova.novamed.dto.auth.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.nova.novamed.util.SessionUtils.requireSession;
import static it.nova.novamed.util.SessionUtils.requireUserId;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // ---------------------------------------------------------
    // GET CURRENT USER
    // ---------------------------------------------------------
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {


        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        User user = authService.getUserById(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("mustChangePassword", user.getMustChangePassword());

        if (user.getDoctor() != null) {
            response.put("doctorId", user.getDoctor().getId());
        }
        if (user.getPatient() != null) {
            response.put("patientId", user.getPatient().getId());
        }
        if (user.getAdmin() != null) {
            response.put("adminId", user.getAdmin().getId());
        }

        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------
    // REGISTER PATIENT
    // ---------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<PatientDto> register(@Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // ---------------------------------------------------------
    // LOGIN (CREA SESSIONE + JSESSIONID)
    // ---------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        HttpSession session = httpRequest.getSession(true);

        LoginResponse response = authService.login(request);

        // Salva i tuoi attributi custom
        session.setAttribute("userId", response.getUserId());
        session.setAttribute("role", response.getRole());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + response.getRole().name()))
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        session.setAttribute("SPRING_SECURITY_CONTEXT", context);

        return ResponseEntity.ok(response);
    }


    // ---------------------------------------------------------
    // CHANGE PASSWORD
    // ---------------------------------------------------------
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        authService.changePassword(userId, req);

        User updated = userRepository.findById(userId).orElseThrow();
        session.setAttribute("user", updated);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
        ));
    }

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok().build();
    }
}