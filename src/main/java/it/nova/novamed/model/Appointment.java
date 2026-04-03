package it.nova.novamed.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(indexes = {
        @Index(columnList = "date"),
        @Index(columnList = "doctor_id")
})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "serviceType_id", nullable = false)
    private ServiceType serviceType;

    @Column(nullable = false)
    private Instant date;

    @Column(nullable = false)
    private Instant dateEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;
    private String patientName;
    private String doctorName;
}
