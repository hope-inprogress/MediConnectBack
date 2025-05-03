package iset.pfe.mediconnectback.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    List<RendezVous> findByMedecinId(Long medecinId);
    List<RendezVous> findByPatientId(Long patientId);

    long countByRendezVousStatut(RendezVousStatut rendezVousStatut);
    List<RendezVous> findByMedecinAndRendezVousStatut(Medecin medecin, RendezVousStatut rendezVousStatut);

    boolean existsByMedecinAndAppointmentTime(Medecin medecin, LocalDateTime appointmentTime);

}
