package iset.pfe.mediconnectback.repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    List<RendezVous> findByMedecinId(Long medecinId);
    List<RendezVous> findByPatientId(Long patientId);

    long countByRendezVousStatut(RendezVousStatut rendezVousStatut);

    List<RendezVous> findByMedecinAndRendezVousStatut(Medecin medecin, RendezVousStatut rendezVousStatut);

    boolean existsByMedecinAndAppointmentTime(Medecin medecin, LocalTime appointmentTime);

    @Query("SELECT rv FROM RendezVous rv WHERE rv.medecin.id = :medecinId AND " +
       "(rv.appointmentDate > CURRENT_DATE OR " +
       "(rv.appointmentDate = CURRENT_DATE AND rv.appointmentTime > CURRENT_TIME)) " +
       "ORDER BY rv.appointmentDate ASC, rv.appointmentTime ASC")
    List<RendezVous> findUpcomingByMedecinId(@Param("medecinId") Long medecinId);
    
    @Query("SELECT DISTINCT rv.patient FROM RendezVous rv " +
           "LEFT JOIN FETCH rv.patient.dossierMedical dm " +
           "LEFT JOIN FETCH dm.fichiers " +
           "WHERE rv.medecin.id = :medecinId")
    List<Patient> findDistinctPatientsByMedecinIdWithDossierMedical(@Param("medecinId") Long medecinId);

    boolean existsByMedecinAndAppointmentDate(Medecin medecin, LocalDate appointmentDate);

  
    List<RendezVous> findByRendezVousStatut(RendezVousStatut rendezVousStatut);

    List<RendezVous> findByRendezVousStatutIn(List<RendezVousStatut> rendezVousStatut);

    List<RendezVous> findByMedecinAndRendezVousStatutIn(Medecin medecin, List<RendezVousStatut> rendezVousStatut);

    List<RendezVous> findByMedecinIdAndAppointmentDateAndRendezVousStatutIn(
    Long medecinId, LocalDate date, List<RendezVousStatut> statusList);

    long count();

    @Query("SELECT rv FROM RendezVous rv " +
        "WHERE rv.medecin.id = :medecinId " +
        "ORDER BY rv.createdAt DESC")
    List<RendezVous> findTop5LatestRendezVousByMedecinId(@Param("medecinId") Long medecinId, Pageable pageable);


}
