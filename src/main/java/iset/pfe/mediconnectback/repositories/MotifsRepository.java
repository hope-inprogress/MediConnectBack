package iset.pfe.mediconnectback.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Motifs;
import iset.pfe.mediconnectback.enums.EventType;

@Repository
public interface MotifsRepository extends JpaRepository<Motifs, Long> {
    List<Motifs> findByTargetUserId(Long userId);

    @Query("SELECT m FROM Motifs m WHERE m.targetUser.id = :userId AND (m.eventType = :eventType1 OR m.eventType = :eventType2) ORDER BY m.eventTime DESC")
    Motifs findTopByTargetUserIdAndEventTypeContainingBlock(@Param("userId") Long userId, @Param("eventType1") EventType eventType1, @Param("eventType2") EventType eventType2);


    
}