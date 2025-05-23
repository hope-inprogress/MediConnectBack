package iset.pfe.mediconnectback.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.DocumentMedical;

@Repository
public interface DocumentMedicalRepository extends JpaRepository<DocumentMedical, Long> {

    List<DocumentMedical> findByDossierMedicalId(Long dossierId);

    List<DocumentMedical> findByDossierMedical_IdAndUploader_Id(Long dossierId, Long uploaderId);

    //List<DocumentMedical> findByDossierMedical_Patient_Id(Long patientId);
    Optional<DocumentMedical> findByDossierMedical_Patient_IdAndFichier(Long patientId, String fileName);

    Optional<DocumentMedical> findByDossierMedicalIdAndFichier(Long id, String string);

    Optional<DocumentMedical> findByUploaderIdAndFichier(Long id, String string);


}
