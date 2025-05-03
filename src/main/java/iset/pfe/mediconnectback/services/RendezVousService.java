package iset.pfe.mediconnectback.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;

@Service
public class RendezVousService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    public RendezVous updateRendezvousStatus(Long id, RendezVousStatut newStatus) {
        RendezVous rendezvous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendezvous with ID " + id + " not found"));

        RendezVousStatut currentStatus = rendezvous.getRendezVousStatut();
        Medecin medecin = rendezvous.getMedecin();
        LocalDateTime appointmentTime = rendezvous.getAppointmentTime();

        boolean isBooked = rendezVousRepository.existsByMedecinAndAppointmentTime(medecin, appointmentTime);

        switch (newStatus) {
            case Confirmed:
                if (!currentStatus.equals(RendezVousStatut.Pending)) {
                    throw new IllegalStateException("Can only confirm a pending appointment");
                }
                if (!medecin.getIsAvailable()) {
                    throw new IllegalStateException("Doctor is not available");
                }
                if (isBooked) {
                    throw new IllegalStateException("This time slot is already booked");
                }
                rendezvous.setRendezVousStatut(RendezVousStatut.Confirmed);
                rendezvous.setCreatedAt(LocalDateTime.now());
                break;

            case Cancelled:
                if (currentStatus.equals(RendezVousStatut.Confirmed)) {
                    rendezvous.setCancelledAt(LocalDateTime.now());
                    rendezvous.setRendezVousStatut(RendezVousStatut.Cancelled);
                }
                break;

            case Completed:
                if (!currentStatus.equals(RendezVousStatut.Confirmed)) {
                    throw new IllegalStateException("Can only complete a confirmed appointment");
                }
                rendezvous.setCompletedAt(LocalDateTime.now());
                rendezvous.setRendezVousStatut(RendezVousStatut.Completed);
                break;

            default:
                throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        return rendezVousRepository.save(rendezvous);
    }

    public List<RendezVous> getAllRendezVous() {
        return rendezVousRepository.findAll();
    }

    public Map<String, Long> getRendezVousStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("Confirmé", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Confirmed));
        stats.put("Rejeté", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Rejected));
        stats.put("En_Attente", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Pending));
        stats.put("Cancellé", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Cancelled));
        stats.put("Completé", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Completed));
        stats.put("No_Show", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.No_Show));
        stats.put("total", rendezVousRepository.count());
        return stats;
    }
}
