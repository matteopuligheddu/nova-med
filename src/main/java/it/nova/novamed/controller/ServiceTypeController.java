package it.nova.novamed.controller;

import it.nova.novamed.dto.service.CreateServiceTypeRequest;
import it.nova.novamed.dto.service.ServiceTypeDto;
import it.nova.novamed.dto.service.UpdateServiceTypeRequest;
import it.nova.novamed.service.ServiceTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static it.nova.novamed.util.SessionUtils.*;

@RestController
@RequestMapping("/api/service-types")
@RequiredArgsConstructor
@Validated
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    // ---------------------------------------------------------
    // CREATE (ADMIN ONLY)
    // ---------------------------------------------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceTypeDto create(
            HttpServletRequest request,
            @Valid @RequestBody CreateServiceTypeRequest body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return serviceTypeService.create(userId, body);
    }

    // ---------------------------------------------------------
    // GET ALL (public)
    // ---------------------------------------------------------
    @GetMapping
    public List<ServiceTypeDto> getAll() {
        return serviceTypeService.getAll();
    }

    // ---------------------------------------------------------
    // GET BY ID (public)
    // ---------------------------------------------------------
    @GetMapping("/{id}")
    public ServiceTypeDto getById(@PathVariable Long id) {
        return serviceTypeService.getById(id);
    }

    // ---------------------------------------------------------
    // UPDATE (ADMIN or DOCTOR OWNER)
    // ---------------------------------------------------------
    @PutMapping("/{serviceTypeId}")
    public ServiceTypeDto update(
            HttpServletRequest request,
            @PathVariable Long serviceTypeId,
            @Valid @RequestBody UpdateServiceTypeRequest body
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return serviceTypeService.update(userId, serviceTypeId, body);
    }

    // ---------------------------------------------------------
    // DELETE (ADMIN ONLY)
    // ---------------------------------------------------------
    @DeleteMapping("/{serviceTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            HttpServletRequest request,
            @PathVariable Long serviceTypeId
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        serviceTypeService.delete(userId, serviceTypeId);
    }

    // ---------------------------------------------------------
    // GET BY DOCTOR (ADMIN or DOCTOR OWNER)
    // ---------------------------------------------------------
    @GetMapping("/doctor/{doctorId}")
    public List<ServiceTypeDto> getByDoctor(
            HttpServletRequest request,
            @PathVariable Long doctorId
    ) {
        HttpSession session = requireSession(request);
        Long userId = requireUserId(session);

        return serviceTypeService.getByDoctor(userId, doctorId);
    }
}