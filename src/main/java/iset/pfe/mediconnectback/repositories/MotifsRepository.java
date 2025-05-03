package iset.pfe.mediconnectback.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Motifs;

@Repository
public interface MotifsRepository extends JpaRepository<Motifs, Long> {
    List<Motifs> findByUserId(Long userId);
    Motifs findTopByUserIdAndEventTypeOrderByEventTimeDesc(Long userId, String eventType);

    
}