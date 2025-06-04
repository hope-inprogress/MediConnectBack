package iset.pfe.mediconnectback.controllers;

import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import iset.pfe.mediconnectback.dtos.FichierMedicalFormDTO;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.FichierMedicalForm;
import iset.pfe.mediconnectback.repositories.DocumentMedicalRepository;
import iset.pfe.mediconnectback.services.FichierMedicalService;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.FileStorageService;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/fichier-medical")
public class FichierMedicalController {

    @Autowired
    private FichierMedicalService fichierMedicalService;

    @Autowired
    private DocumentMedicalRepository documentMedicalRepository;
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private FileStorageService fileStorageService;

    
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/fill")
    public ResponseEntity<?> fillMedicalForm(@RequestHeader("Authorization") String token,
                                             @RequestBody FichierMedicalFormDTO dto) {
        try {
            Long patientId = jwtService.extractIdFromBearer(token);
            fichierMedicalService.fillAndGenerateMedicalForm(patientId, dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Medical form updated successfully");
            response.put("timestamp", LocalDateTime.now());
            response.put("patientId", patientId);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update medical form: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        try {
            FichierMedicalForm form = fichierMedicalService.getFichierMedicalFormByPatientId(patientId);

            double bmi = form.getWeight() / Math.pow(form.getHeight() / 100, 2);

            Map<String, Object> stats = new HashMap<>();
            stats.put("bloodType", form.getBloodType() != null ? form.getBloodType().toString() : "Unknown");
            stats.put("bmi", Math.round(bmi * 10.0) / 10.0);
            stats.put("smoker", form.getSmoker() != null ? form.getSmoker() : "Unknown");
            stats.put("alcoholUse", form.getAlcoholUse() != null ? form.getAlcoholUse() : "Unknown");
            stats.put("activityLevel", form.getActivityLevel() != null ? form.getActivityLevel().toString() : "Unknown");
            stats.put("dietaryPreferences", form.getDietaryPreferences() != null ? form.getDietaryPreferences() : "Unknown");

            return ResponseEntity.ok(stats);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getReason()));
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/get-form")
    public ResponseEntity<FichierMedicalFormDTO> getMedicalForm(@RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        try {
            FichierMedicalForm form = fichierMedicalService.getFichierMedicalFormByPatientId(patientId);
            FichierMedicalFormDTO dto = new FichierMedicalFormDTO();
            dto.setBloodType(form.getBloodType());
            dto.setHeight(form.getHeight());
            dto.setWeight(form.getWeight());
            dto.setAllergies(form.getAllergies());
            dto.setChronicDiseases(form.getChronicDiseases());
            dto.setCurrentMedications(form.getCurrentMedications());
            dto.setSurgicalHistory(form.getSurgicalHistory());
            dto.setFamilyMedicalHistory(form.getFamilyMedicalHistory());
            dto.setSmoker(form.getSmoker());
            dto.setAlcoholUse(form.getAlcoholUse());
            dto.setActivityLevel(form.getActivityLevel());
            dto.setDietaryPreferences(form.getDietaryPreferences());

            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
        }
    }
}

