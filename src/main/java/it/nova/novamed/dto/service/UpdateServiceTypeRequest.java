package it.nova.novamed.dto.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateServiceTypeRequest {

    @NotNull
    private Long doctorId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @PositiveOrZero
    private Integer price;

    @NotNull
    @Positive
    private Integer durationMinutes;
}
