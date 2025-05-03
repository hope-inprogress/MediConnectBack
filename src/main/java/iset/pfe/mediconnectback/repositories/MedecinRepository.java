package iset.pfe.mediconnectback.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Medecin;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    Optional<Medecin> findMedecinById(Long id);

    @Query("SELECT COUNT(m) FROM Medecin m WHERE MONTH(m.createdDate) = :month")
    Long countMedecinByMonth(@Param("month") int month);   
}
