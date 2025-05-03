package iset.pfe.mediconnectback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT COUNT(m) FROM Patient m WHERE MONTH(m.createdDate) = :month")
    Long countPatientsByMonth(@Param("month") int month);

}