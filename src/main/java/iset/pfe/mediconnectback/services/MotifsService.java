package iset.pfe.mediconnectback.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Motifs;
import iset.pfe.mediconnectback.repositories.MotifsRepository;

@Service
public class MotifsService {
    
    @Autowired
    private MotifsRepository motifsRepository;

    
    public List<Motifs> getMotifsDetailsByUserId(Long userId) {
        return motifsRepository.findByUserId(userId);
    }
  
    public LocalDate getLastBlockDate(Long userId) {
        Motifs lastMotif = motifsRepository.findTopByUserIdAndEventTypeOrderByEventTimeDesc(userId, "block");
        if (lastMotif != null && "block".equals(lastMotif.getEventType())) {
            return lastMotif.getEventTime(); // Return the date in String format
        }
            return null; // Return null if there's no block
}
    
}
