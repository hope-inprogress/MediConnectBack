package iset.pfe.mediconnectback.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Motifs;
import iset.pfe.mediconnectback.enums.EventType;
import iset.pfe.mediconnectback.repositories.MotifsRepository;
import iset.pfe.mediconnectback.dtos.MotifDto;



@Service
public class MotifsService {
    
    @Autowired
    private MotifsRepository motifsRepository;

    
    public List<MotifDto> getMotifsDetailsByUserId(Long userId) {
        List<Motifs> motifs = motifsRepository.findByTargetUserId(userId);

        List<MotifDto> motifsDtoList = motifs.stream().map(motif -> {
            MotifDto motifDto = new MotifDto();
            motifDto.setId(motif.getId());
            motifDto.setEventType(motif.getEventType());
            motifDto.setEventTime(motif.getEventTime());
            motifDto.setReason(motif.getReason());
            motifDto.setDescription(motif.getDescription());
            motifDto.setTargetUserId(motif.getTargetUserId());
            //motifDto.setPerformedById(motif.getPerformedBy().getId());
            return motifDto;
        }).toList();

        return motifsDtoList;

    }
  
    public LocalDate getLastBlockDate(Long userId) {

        Motifs lastMotif = motifsRepository.findTopByTargetUserIdAndEventTypeContainingBlock(userId, EventType.PATIENT_BLOCKED_BY_MEDECIN, EventType.USER_BLOCKED_BY_ADMIN);
        if (lastMotif != null) {
            return lastMotif.getEventTime(); // Return the date in String format
        }
            return null; // Return null if there's no block
    }
    
}
