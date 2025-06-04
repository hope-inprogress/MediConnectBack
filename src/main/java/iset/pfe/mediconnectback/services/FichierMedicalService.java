package iset.pfe.mediconnectback.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import iset.pfe.mediconnectback.dtos.FichierMedicalFormDTO;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.FichierMedicalForm;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.enums.DocumentVisibility;
import iset.pfe.mediconnectback.repositories.DocumentMedicalRepository;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.repositories.FichierMedicalFormRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FichierMedicalService {

    @Autowired
    private FichierMedicalFormRepository fichierMedicalFormRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    @Autowired
    private DocumentMedicalRepository documentMedicalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public FichierMedicalForm getFichierMedicalFormByPatientId(Long patientId) {
        return fichierMedicalFormRepository.findByPatientId(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier médical introuvable"));
    }

    
    @Transactional
    public void fillAndGenerateMedicalForm(Long patientId, FichierMedicalFormDTO dto) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
            .orElseThrow(() -> new EntityNotFoundException("Dossier not found"));

        FichierMedicalForm form = fichierMedicalFormRepository.findByPatientId(patientId)
            .orElse(new FichierMedicalForm());

        // Set fields
        form.setHeight(dto.getHeight());
        form.setWeight(dto.getWeight());
        form.setBloodType(dto.getBloodType());
        form.setAllergies(dto.getAllergies());
        form.setChronicDiseases(dto.getChronicDiseases());
        form.setCurrentMedications(dto.getCurrentMedications());
        form.setSurgicalHistory(dto.getSurgicalHistory());
        form.setFamilyMedicalHistory(dto.getFamilyMedicalHistory());
        form.setSmoker(dto.getSmoker());
        form.setAlcoholUse(dto.getAlcoholUse());
        form.setActivityLevel(dto.getActivityLevel());
        form.setDietaryPreferences(dto.getDietaryPreferences());
        form.setPatient(patient);
        form.setDossierMedical(dossier);

        fichierMedicalFormRepository.save(form);

        // Generate PDF
        byte[] pdfBytes = pdfGeneratorService.generateMedicalFormPdf(form);

        // Save PDF to filesystem
        String fileName = "MedicalForm_" + patientId + ".pdf";
        MultipartFile multipartFile = new MockMultipartFile(
            fileName,
            fileName,
            "application/pdf",
            pdfBytes
        );

        // Find and delete previous document if exists
        DocumentMedical existingDoc = documentMedicalRepository.findByDossierMedical_Patient_IdAndFichier(patientId, fileName)
            .orElse(null);
        
        if (existingDoc != null) {
            try {
                // Delete the file from filesystem
                Files.deleteIfExists(Paths.get(existingDoc.getFichier().substring(1))); // Remove leading slash
                // Delete from database
                documentMedicalRepository.delete(existingDoc);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete old PDF file", e);
            }
        }

        // Store new PDF
        String storedPath = fileStorageService.storeFile(multipartFile);

        // Create new document
        DocumentMedical newDoc = new DocumentMedical();
        newDoc.setFichier(storedPath);
        newDoc.setType("application/pdf");
        newDoc.setDossierMedical(dossier);
        newDoc.setUploader(patient);
        newDoc.setCreatedAt(LocalDateTime.now());
        newDoc.setVisibility(DocumentVisibility.PUBLIC);

        documentMedicalRepository.save(newDoc);
    }

    
    public DocumentMedical getPdfByPatientId(Long patientId) {
        FichierMedicalForm form = getFichierMedicalFormByPatientId(patientId);
        DocumentMedical document = documentMedicalRepository.findByDossierMedicalIdAndFichier(form.getDossierMedical().getId(), "MedicalForm.pdf")
            .orElseThrow(() -> new EntityNotFoundException("PDF not found for patient ID: " + patientId));

        return document;
    }
}
