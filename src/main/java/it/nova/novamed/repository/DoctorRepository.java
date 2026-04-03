package it.nova.novamed.repository;

import it.nova.novamed.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    void deleteByUser_Id(Long userId);

    Optional<Doctor> findByUser_Id(Long userId);
}