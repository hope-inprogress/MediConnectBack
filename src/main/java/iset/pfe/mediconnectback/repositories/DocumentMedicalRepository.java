package iset.pfe.mediconnectback.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.RendezVous;

@Repository
public interface DocumentMedicalRepository extends JpaRepository<DocumentMedical, Long> {

    List<DocumentMedical> findByDossierMedicalId(Long dossierId);

    List<DocumentMedical> findByDossierMedical_IdAndUploader_Id(Long dossierId, Long uploaderId);

        @Query("SELECT dm FROM DocumentMedical dm " +
        "WHERE dm.dossierMedical.id = :dossierId " +
        "ORDER BY dm.createdAt DESC")
    List<DocumentMedical> findTop5LatestByDossierMedicalIdOrderByCreatedAtDesc(@Param("dossierId") Long dossierId);

    //List<DocumentMedical> findByDossierMedical_Patient_Id(Long patientId);
    Optional<DocumentMedical> findByDossierMedical_Patient_IdAndFichier(Long patientId, String fileName);

    Optional<DocumentMedical> findByDossierMedicalIdAndFichier(Long id, String string);

    Optional<DocumentMedical> findByUploaderIdAndFichier(Long id, String string);

    List<DocumentMedical> findByUploader_Id(Long uploaderId);



}
