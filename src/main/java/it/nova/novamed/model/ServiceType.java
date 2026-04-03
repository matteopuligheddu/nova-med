package it.nova.novamed.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; //Tipologia visita
    private String description;
    private Integer price;
    private Integer durationMinutes;


    @ManyToOne
    private Doctor doctor;
}