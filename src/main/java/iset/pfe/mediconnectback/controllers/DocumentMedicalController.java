package iset.pfe.mediconnectback.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import org.springframework.core.io.Resource;  // Spring's Resource interface
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import iset.pfe.mediconnectback.dtos.DocumentMedicalDto;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.services.DocumentMedicalService;
import iset.pfe.mediconnectback.services.JwtService;
import org.springframework.transaction.annotation.Transactional;  // ✅ Spring's (has readOnly)


@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentMedicalController {

    @Autowired
    private DocumentMedicalService fileService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DossierMedicalRepository dossierRepository;

 // Download a document by its ID and the user id
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('MEDECIN', 'PATIENT')")
    @GetMapping("/{docId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long docId
    ) {

        Resource resource = fileService.downloadDocument(docId);
        if (!resource.exists() || !resource.isReadable()) {
            System.out.println("File does not exist or is not readable");
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(null);
        }

        String fileName = resource.getFilename();
        String contentType = "application/octet-stream";
        String fileExtension = "";
        if (fileName != null && fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            switch (fileExtension) {
                case "pdf":
                    contentType = "application/pdf";
                    break;
                case "doc":
                case "docx":
                    contentType = "application/msword";
                    break;
                default:
                    contentType = "application/octet-stream";
            }
        }

        System.out.println("Serving file with content type: " + contentType);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    } 
    
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/{dossierId}")
    public ResponseEntity<List<DocumentMedicalDto>> getDocumentsByDossierForMedecin(@PathVariable Long dossierId, @RequestHeader("Authorization") String authHeader) {
        Long medecinId = jwtService.extractIdFromBearer(authHeader);
        List<DocumentMedicalDto> docs = fileService.getDocumentsByDossierForMedecin(dossierId, medecinId);

        return ResponseEntity.ok(docs);
    }

    // Upload a document for a specific dossier By Patient Or by Medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/{dossierId}/upload")
    public ResponseEntity<String> uploadFile(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false) String type,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long uploaderId = jwtService.extractIdFromBearer(authHeader);

        try {
             String path = fileService.uploadDocument(
                dossierId, 
                uploaderId,
                file, 
                type,
                null, // Default visibility to null
                null
                );
            return ResponseEntity.ok("File uploaded: " + path);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload file: " + e.getMessage());
        }  
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/all")
    public ResponseEntity<List<DocumentMedicalDto>> getDocumentsForPatient(@RequestHeader("Authorization") String authHeader) {
        Long patientId = jwtService.extractIdFromBearer(authHeader);

        List<DocumentMedicalDto> docs = fileService.getDocumentsByDossierForPatient(patientId);

        return ResponseEntity.ok(docs);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/recent")
    public ResponseEntity<List<DocumentMedicalDto>> getRecentDocumentsForPatient(@RequestHeader("Authorization") String authHeader) {
        Long patientId = jwtService.extractIdFromBearer(authHeader);

        List<DocumentMedicalDto> recentDocs = fileService.getRecentDocumentsForPatient(patientId);

        return ResponseEntity.ok(recentDocs);
    }

        // Upload a document for a specific dossier By Patient Or by Medecin
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFileForPatient(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam("visibility") String visibility,
            @RequestParam(value = "allowedMedecinIds", required = false) List<Long> allowedMedecinIds,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long uploaderId = jwtService.extractIdFromBearer(authHeader);

        DossierMedical dossier = dossierRepository.findByPatientId(uploaderId)
                            .orElseThrow(() -> new RuntimeException("Dossier not found for patient"));

        try {
             String path = fileService.uploadDocument(
                dossier.getId(), 
                uploaderId,
                file, 
                type,
                visibility,
                allowedMedecinIds
                );
            return ResponseEntity.ok("File uploaded: " + path);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload file: " + e.getMessage());
        }  
    }

    // Update document visibility or allowed medecins
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/{docId}/update")
    public ResponseEntity<String> updateDocument(
            @PathVariable Long docId,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "allowedMedecinIds", required = false) List<Long> allowedMedecinIds,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = jwtService.extractIdFromBearer(authHeader);
        fileService.updateDocument(docId, userId, visibility, allowedMedecinIds);
        return ResponseEntity.ok("Document updated successfully");
    }

    // Delete a document by its ID and the user id
    @PreAuthorize("hasAnyRole('MEDECIN', 'PATIENT')")
    @DeleteMapping("/{docId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable Long docId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = jwtService.extractIdFromBearer(authHeader);
        fileService.deleteDocument(docId, userId);
        return ResponseEntity.ok("Document deleted");
    }
}
