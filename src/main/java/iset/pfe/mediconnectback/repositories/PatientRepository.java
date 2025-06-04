package iset.pfe.mediconnectback.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT COUNT(m) FROM Patient m WHERE MONTH(m.createdDate) = :month")
    Long countPatientsByMonth(@Param("month") int month);

    @Query("SELECT p FROM Patient p  JOIN p.mesMedecins m WHERE m.id = :medecinId")
    List<Patient> findPatientsByMedecinId(@Param("medecinId") Long medecinId);

    @Query("SELECT p FROM Patient p JOIN p.mesMedecins m WHERE m.id = :medecinId")
    List<Patient> find5TopPatientsByMedecinId(@Param("medecinId") Long medecinId);

    Optional<Patient> findPatientById(Long id);

            @Query("""
        SELECT p FROM Medecin m
        JOIN m.mesPatients p
        WHERE m.id = :userId
          AND LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Patient> searchForAPatient(@Param("userId") Long userId, @Param("name") String name);

}