package iset.pfe.mediconnectback.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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

import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.services.DocumentMedicalService;
import iset.pfe.mediconnectback.services.JwtService;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentMedicalController {

    @Autowired
    private DocumentMedicalService fileService;

    @Autowired
    private JwtService jwtService;

    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/{dossierId}")
    public ResponseEntity<List<DocumentMedical>> getAllDocuments(@PathVariable Long dossierId) {
        return ResponseEntity.ok(fileService.getAllDocuments(dossierId));
    }

    //get documents for a patients:
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DocumentMedical>> getDocumentsByPatientId(@PathVariable Long patientId, @RequestHeader("Authorization") String authHeader) {
        Long medecinId = jwtService.extractIdFromBearer(authHeader);
        List<DocumentMedical> documents = fileService.getDocumentsByPatientId(patientId, medecinId);
        return ResponseEntity.ok(documents);
    }


    // get documents by dossierId and if it's private see if the medecin is one of th medecins who can see it

    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/{dossierId}/upload")
    public ResponseEntity<String> uploadFile(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false) String type,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long medecinId = jwtService.extractIdFromBearer(authHeader);

        String path = fileService.uploadDocument(dossierId, medecinId, file, type);
        return ResponseEntity.ok("File uploaded: " + path);
    }

    // Download a document by its ID and the user id
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/{docId}/medecin")
    public ResponseEntity<Resource> downloadDocumentAsMedecin(
            @PathVariable Long docId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = jwtService.extractIdFromBearer(authHeader);

        Resource resource = fileService.downloadDocument(docId, userId);

        String fileName = resource.getFilename();
        String contentType = "application/octet-stream";

        if (fileName != null) {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            contentType = switch (ext) {
                case "pdf" -> "application/pdf";
                case "doc", "docx" -> "application/msword";
                case "png" -> "image/png";
                case "jpg", "jpeg" -> "image/jpeg";
                default -> contentType;
            };
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);

    }

    @PreAuthorize("hasRole('MEDECIN')")
    @DeleteMapping("/{docId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable Long docId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long medecinId = jwtService.extractIdFromBearer(authHeader);
        fileService.deleteDocument(docId, medecinId);
        return ResponseEntity.ok("Document deleted");
    }
}
