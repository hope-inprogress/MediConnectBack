package iset.pfe.mediconnectback.services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.dtos.DocumentMedicalDto;
import iset.pfe.mediconnectback.dtos.DossierMedicalDTO;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.UserRole;
import jakarta.transaction.Transactional;

@Service
public class DossierMedicalService {

    @Autowired
    private MedecinService medecinServcie;

    @Transactional
    public List<DossierMedicalDTO> getDossiersForMedecin(Long medecinId) {
        Medecin medecin = medecinServcie.getMedecinById(medecinId);
        
        return medecin.getMesPatients().stream()
            .map(patient -> {
                DossierMedical dossier = patient.getDossierMedical();
                if (dossier == null) {
                    return null;
                }

                DossierMedicalDTO dto = new DossierMedicalDTO();
                dto.setId(dossier.getId());
                dto.setDateCreated(dossier.getDateCreated());
                dto.setPatientName(patient.getFullName());
                dto.setPatientImage(patient.getImageUrl());

                // Convert documents to DTOs
                List<DocumentMedicalDto> documentDTOs = dossier.getFichiers().stream()
                    .map(doc -> convertToDocumentMedicalDTO(doc, medecin))
                    .collect(Collectors.toList());
                dto.setFichiers(documentDTOs);

                return dto;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private DocumentMedicalDto convertToDocumentMedicalDTO(DocumentMedical document, User currentUser) {
        DocumentMedicalDto dto = new DocumentMedicalDto();
        dto.setId(document.getId());
        dto.setType(document.getType());
        dto.setFichier(document.getFichier());
        dto.setUploadDate(document.getCreatedAt());
        dto.setVisibility(document.getVisibility().name());
        
        // Set uploader name and image based on current user comparison
        if (document.getUploader().getId().equals(currentUser.getId())) {
            if (currentUser.getRole().equals(UserRole.Patient)) {
                dto.setUploaderName("Vous (Patient)" );
            } else {
                dto.setUploaderName("Vous (Médecin)");
            }
        } else {
            dto.setUploaderName(document.getUploader().getFirstName() + " " + document.getUploader().getLastName());
            dto.setUploaderImage(document.getUploader().getImageUrl());
        }
        
        return dto;
    }

}
