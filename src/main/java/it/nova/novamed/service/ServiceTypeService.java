package it.nova.novamed.service;

import it.nova.novamed.dto.service.CreateServiceTypeRequest;
import it.nova.novamed.dto.service.ServiceTypeDto;
import it.nova.novamed.dto.service.UpdateServiceTypeRequest;

import java.util.List;

public interface ServiceTypeService {

    List<ServiceTypeDto> getAll();

    List<ServiceTypeDto> getByDoctor(Long userId, Long doctorId);

    ServiceTypeDto getById(Long id);

    ServiceTypeDto create(Long userId, CreateServiceTypeRequest request);

    ServiceTypeDto update(Long userId, Long serviceTypeId, UpdateServiceTypeRequest request);

    void delete(Long userId, Long serviceTypeId);
}