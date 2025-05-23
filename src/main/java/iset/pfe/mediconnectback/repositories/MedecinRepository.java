package iset.pfe.mediconnectback.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Medecin;
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
}
