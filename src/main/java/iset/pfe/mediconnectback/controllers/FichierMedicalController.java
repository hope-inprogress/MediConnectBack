package iset.pfe.mediconnectback.controllers;

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

import iset.pfe.mediconnectback.dtos.FichierMedicalFormDTO;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.repositories.DocumentMedicalRepository;
import iset.pfe.mediconnectback.services.FichierMedicalService;
import iset.pfe.mediconnectback.services.JwtService;
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

    
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/fill")
    public ResponseEntity<?> fillMedicalForm(@RequestHeader("Authorization") String token,
                                             @RequestBody FichierMedicalFormDTO dto) {


        Long patientId = jwtService.extractIdFromBearer(token);

        fichierMedicalService.fillAndGenerateMedicalForm(patientId, dto);
        return ResponseEntity.ok("Form saved and PDF generated");
    }

    @GetMapping("/download")
public ResponseEntity<byte[]> downloadMedicalForm(@RequestHeader("Authorization") String token) {
    Long patientId = jwtService.extractIdFromBearer(token);
    DocumentMedical doc = documentMedicalRepository.findByDossierMedical_Patient_IdAndFichier(patientId, "MedicalForm.pdf")
        .orElseThrow(() -> new EntityNotFoundException("Document not found"));

    byte[] pdfData = doc.getFileContent();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.inline().filename("MedicalForm.pdf").build());

    return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
}

}

