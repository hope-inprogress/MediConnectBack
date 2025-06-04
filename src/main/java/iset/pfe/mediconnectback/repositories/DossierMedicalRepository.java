package iset.pfe.mediconnectback.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.DossierMedical;

@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {

    @Query("SELECT dm FROM DossierMedical dm JOIN FETCH dm.patient WHERE dm.patient.id = :patientId")
    Optional<DossierMedical> findByPatientId(@Param("patientId") Long patientId);
    
    /*@Query("SELECT dm FROM DossierMedical dm JOIN FETCH dm.patient p JOIN FETCH dm.fichiers f JOIN FETCH dm.medecin m WHERE dm.medecin.id = :medecinId")
    List<DossierMedical> findByMedecinId(@Param("medecinId") Long medecinId);*/

   @Query("SELECT dm FROM DossierMedical dm JOIN FETCH dm.patient p LEFT JOIN FETCH dm.fichiers WHERE p.id = :patientId")
    DossierMedical findByPatientIdWithFichiers(@Param("patientId") Long patientId);
}