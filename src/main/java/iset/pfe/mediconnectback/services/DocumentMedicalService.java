package iset.pfe.mediconnectback.services;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.repositories.DocumentMedicalRepository;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;

@Service
public class DocumentMedicalService {
    
    @Autowired
    private DocumentMedicalRepository documentRepo;

    @Autowired
    private DossierMedicalRepository dossierRepo;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MedecinService medecinService;

    
    public String uploadDocument(Long dossierId, Long medecinId, MultipartFile file, String type) {
        DossierMedical dossier = dossierRepo.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier not found"));

        Medecin medecin = medecinService.getMedecinById(medecinId);

        if (file.isEmpty()) {
            throw new RuntimeException("Cannot store empty file ");
        }
        
        String storedPath = fileStorageService.storeFile(file);

        DocumentMedical document = new DocumentMedical();
        document.setType(type != null ? type : "Unknown");
        document.setFichier(storedPath);
        document.setCreatedAt(LocalDateTime.now());
        document.setDossierMedical(dossier);
        document.setMedecin(medecin);

        documentRepo.save(document);
        return storedPath;
    }

    // Get all documents for a specific dossier
    public List<DocumentMedical> getAllDocuments(Long dossierId) {
        DossierMedical dossier = dossierRepo.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier not found"));
        return dossier.getFichiers();
    }

    public void deleteDocument(Long docId, Long medecinId) {
        DocumentMedical doc = documentRepo.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getMedecin().getId().equals(medecinId)) {
            throw new RuntimeException("You can only delete documents uploaded by you");
        }

        documentRepo.delete(doc);
    }

    public Resource downloadDocument(Long docId, Long userId) {
        DocumentMedical doc = documentRepo.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getMedecin().getId().equals(userId) || !doc.getDossierMedical().getMedecin().getId().equals(userId)) {
            throw new RuntimeException("You can only download documents from your own dossier");
        }

        String fileName = Paths.get(doc.getFichier()).getFileName().toString();
 
        return fileStorageService.loadFileAsResource(fileName);
    }

}
