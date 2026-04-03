package it.nova.novamed.service;



import it.nova.novamed.dto.calendar.SlotDto;

import java.time.LocalDate;
import java.util.List;

public interface SlotService {

    List<SlotDto> generateSlots(Long userId, Long doctorId, LocalDate date, Long serviceTypeId);
}