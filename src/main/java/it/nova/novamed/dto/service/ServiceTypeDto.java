package it.nova.novamed.dto.service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceTypeDto {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private Long doctorId;
    private Integer durationMinutes;
}
