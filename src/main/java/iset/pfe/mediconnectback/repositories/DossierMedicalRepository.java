package iset.pfe.mediconnectback.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.DossierMedical;

@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Integer> {
    @Query("SELECT dm FROM DossierMedical dm JOIN FETCH dm.patient WHERE dm.patient.id = :patientId")
    List<DossierMedical> findByPatientId(@Param("patientId") Long patientId);
    @Query("SELECT dm FROM DossierMedical dm JOIN FETCH dm.patient p JOIN FETCH dm.fichiers f JOIN FETCH dm.medecin m WHERE dm.medecin.id = :medecinId")
    List<DossierMedical> findByMedecinId(@Param("medecinId") Long medecinId);
}
