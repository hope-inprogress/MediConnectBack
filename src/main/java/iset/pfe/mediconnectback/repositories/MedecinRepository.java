package iset.pfe.mediconnectback.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;
import jakarta.transaction.Transactional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    Optional<Medecin> findMedecinById(Long id);

    Optional<Medecin> findMedecinByCodeMedical(String codeMedical);

    @Query("SELECT COUNT(m) FROM Medecin m WHERE MONTH(m.createdDate) = :month")
    Long countMedecinByMonth(@Param("month") int month);  
     
    @Modifying
    @Transactional
    @Query("UPDATE Medecin m SET m.autoManageAppointments = :autoManage WHERE m.id = :id")
    int updateAutoManageAppointments(@Param("id") Long id, @Param("autoManage") boolean autoManage);

    @Query("SELECT m FROM Medecin m JOIN m.mesPatients p WHERE p.id = :patientId")
    List<Medecin> findByPatientId(Long patientId);

       @Query("""
        SELECT m FROM Patient p
        JOIN p.mesMedecins m
        WHERE p.id = :userId
          AND LOWER(CONCAT(m.firstName, ' ', m.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Medecin> searchForAMedecin(@Param("name") String name, @Param("userId") Long userId);
    
}
