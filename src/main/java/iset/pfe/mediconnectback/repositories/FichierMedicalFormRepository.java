package iset.pfe.mediconnectback.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.FichierMedicalForm;

@Repository
public interface FichierMedicalFormRepository extends JpaRepository<FichierMedicalForm, Long> {
    Optional<FichierMedicalForm> findByPatientId(Long patientId);
    Optional<FichierMedicalForm> findByIdAndPatientId(Long id, Long patientId);



}
