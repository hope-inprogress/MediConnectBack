package iset.pfe.mediconnectback.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import iset.pfe.mediconnectback.dtos.DocumentMedicalDto;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.DocumentVisibility;
import iset.pfe.mediconnectback.repositories.DocumentMedicalRepository;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import jakarta.transaction.Transactional;

@Service
public class DocumentMedicalService {
    
    @Autowired
    private DocumentMedicalRepository documentRepo;

    @Autowired
    private DossierMedicalRepository dossierRepo;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private MedecinService medecinService;

    @Transactional
    public String uploadDocument(Long dossierId, Long uploaderId, MultipartFile file, String type, 
                                String visibilityStr, List<Long> allowedMedecinIds) {
        DossierMedical dossier = dossierRepo.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier not found"));

        Patient patient = dossier.getPatient();
        User uploader = userService.findById(uploaderId); //genericUSer
        List<Medecin> allPatientMedecins = patient.getMesMedecins();

        if (file.isEmpty()) {
            throw new RuntimeException("Cannot store empty file ");
        }
        
        String storedPath = fileStorageService.storeFile(file);

        DocumentMedical document = new DocumentMedical();
        document.setType(type != null ? type : "Unknown");
        document.setFichier(storedPath);
        document.setCreatedAt(LocalDateTime.now());
        document.setDossierMedical(dossier);
        document.setUploader(uploader);

        DocumentVisibility visibility = DocumentVisibility.valueOf(visibilityStr.toUpperCase());
        document.setVisibility(visibility);

        // Handle allowed medecins
        List<Medecin> allowed = new ArrayList<>();

        if (visibility == DocumentVisibility.PUBLIC) {
        allowed.addAll(allPatientMedecins); // PUBLIC: all medecins can view
        } else { //Private
            if (uploader instanceof Patient) {
                // Patient uploading a private document
                if (allowedMedecinIds != null && !allowedMedecinIds.isEmpty()) {
                    for (Long medecinId : allowedMedecinIds) {
                        allowed.add(medecinService.getMedecinById(medecinId));
                    }       
                }
            } else if (uploader instanceof Medecin) {
                // Medecin uploading a private document
                Medecin medecinUploader = (Medecin) uploader;
                allowed.add(medecinUploader); // always allow uploader

                /*if (allowedMedecinIds != null && !allowedMedecinIds.isEmpty()) {
                    for (Long id : allowedMedecinIds) {
                        if (!id.equals(medecinUploader.getId())) {
                            allowed.add(medecinService.getMedecinById(id));
                        }
                    }       
                }*/
            }
        }
        document.setAllowedMedecins(allowed);
        documentRepo.save(document);

        return storedPath;
    }

    // Get all documents for a specific dossier
    @Transactional
    public List<DocumentMedical> getAllDocuments(Long dossierId) {
        return documentRepo.findByDossierMedicalId(dossierId);
    }

    @Transactional
    public void deleteDocument(Long docId, Long userId) {
        DocumentMedical doc = documentRepo.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getUploader().getId().equals(userId)) {
            throw new RuntimeException("You can only delete documents uploaded by you");
        }
        DossierMedical dossier = doc.getDossierMedical();
         // Delete the file from the filesystem
        try {
            Files.deleteIfExists(Paths.get(doc.getFichier().substring(1)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage());    
        }

        // Remove the document from the dossier's fichiers list
        dossier.getFichiers().remove(doc);
        dossierRepo.save(dossier);
        documentRepo.delete(doc);
    }

    public Resource downloadDocument(Long docId) {
        DocumentMedical doc = documentRepo.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String fileName = Paths.get(doc.getFichier()).getFileName().toString();
 
        return fileStorageService.loadFileAsResource(fileName);
    }

    /*public List<DocumentMedical> getDocumentsByPatientId(Long patientId) {
        return documentRepo.findByDossierMedical_Patient_Id(patientId);
    }*/

    @Transactional
    public List<DocumentMedicalDto> getDocumentsByDossierForMedecin(Long dossierId, Long userId) {
        List<DocumentMedical> allDocuments = getAllDocuments(dossierId);
        // Optional: confirm uploader is a Medecin (type check if needed)

        return allDocuments.stream()
            .filter(doc -> 
            "public".equalsIgnoreCase(doc.getVisibility().name()) ||
            Objects.equals(doc.getUploader().getId(), userId) ||
            (doc.getAllowedMedecins() != null && doc.getAllowedMedecins().stream().anyMatch(med -> med.getId().equals(userId)))
            )
            .map(this::toDto) // Convert to DTO
            .collect(Collectors.toList());
    }

    @Transactional
    public List<DocumentMedicalDto> getDocumentsByDossierForPatient(Long dossierId) {
        
        List<DocumentMedical> documents = getAllDocuments(dossierId); // eager fetch allowedMedecins

        return documents.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public DocumentMedicalDto toDto(DocumentMedical doc) {
        DocumentMedicalDto dto = new DocumentMedicalDto();
        dto.setId(doc.getId());
        dto.setFichier(doc.getFichier());
        dto.setType(doc.getType());
        dto.setVisibility(doc.getVisibility().name());
        dto.setUploaderId(doc.getUploader().getId());
        dto.setUploadDate(doc.getCreatedAt()); // or whatever timestamp you use
        return dto;
    }




}
