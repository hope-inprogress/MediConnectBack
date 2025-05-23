package iset.pfe.mediconnectback.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.dtos.FichierMedicalFormDTO;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.FichierMedicalForm;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.repositories.DocumentMedicalRepository;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.repositories.FichierMedicalFormRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

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

    public FichierMedicalForm getFichierMedicalFormByPatientId(Long patientId) {
        return fichierMedicalFormRepository.findByPatientId(patientId)
            .orElseThrow(() -> new EntityNotFoundException("Fichier medical form not found for patient ID: " + patientId));
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

        // Save as DocumentMedical
        DocumentMedical doc = dossier.getFichiers().stream()
            .filter(d -> d.getFichier().equals("MedicalForm.pdf"))
            .findFirst()
            .orElse(new DocumentMedical());

        doc.setFichier("MedicalForm.pdf");
        doc.setFileContent(pdfBytes);
        doc.setType("application/pdf");
        doc.setDossierMedical(dossier);
        doc.setUploader(patient);
        doc.setCreatedAt(LocalDateTime.now());

        documentMedicalRepository.save(doc);
    }

    
    public DocumentMedical getPdfByPatientId(Long patientId) {
        FichierMedicalForm form = getFichierMedicalFormByPatientId(patientId);
        DocumentMedical document = documentMedicalRepository.findByDossierMedicalIdAndFichier(form.getDossierMedical().getId(), "MedicalForm.pdf")
            .orElseThrow(() -> new EntityNotFoundException("PDF not found for patient ID: " + patientId));

        return document;
    }
}
