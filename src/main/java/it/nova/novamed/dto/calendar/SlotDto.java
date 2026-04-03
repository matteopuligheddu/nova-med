package it.nova.novamed.dto.calendar;

import java.time.LocalTime;

public record SlotDto(LocalTime time, boolean free) {}