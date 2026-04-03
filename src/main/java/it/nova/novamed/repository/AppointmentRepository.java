package it.nova.novamed.repository;

import it.nova.novamed.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.time.Instant;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndDateBetween(Long doctorId, Instant start, Instant end);

    boolean existsByDoctor_IdAndDate(Long doctorId, Instant date);

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.status <> 'CANCELLED'
        AND a.date < :end
        AND a.dateEnd > :start
    """)
    boolean overlaps(Long doctorId, Instant start, Instant end);

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.id <> :appointmentId
        AND a.status <> 'CANCELLED'
        AND a.date < :end
        AND a.dateEnd > :start
    """)
    boolean overlapsExceptId(Long appointmentId, Long doctorId, Instant start, Instant end);
}
