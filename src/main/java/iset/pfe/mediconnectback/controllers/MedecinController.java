package iset.pfe.mediconnectback.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Note;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.MedecinService;
import iset.pfe.mediconnectback.services.UserService;


@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/api/medecins")
public class MedecinController {
    
    @Autowired
    private MedecinService medecinService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired

    // Get all medecins (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Medecin> getAllMedecins() {
        return medecinService.getAllMedecins();
    }

    // Get all patients for a specific medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/me/patients")
    public ResponseEntity<List<Patient>> getPatientsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<Patient> patients = medecinService.getPatientsByMedecin(medecinId);
        return ResponseEntity.ok(patients);
    }

    // Get all appointments (RendezVous) for a specific medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/me/appointments")
    public ResponseEntity<List<RendezVous>> getAppointmentsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<RendezVous> appointments = medecinService.getAppointmentsByMedecin(medecinId);
        return ResponseEntity.ok(appointments);
    }
    








    // Add a private note for a medecin (only the medecin can add notes to their profile)
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/notes")
    public ResponseEntity<String> addPrivateNote(@RequestHeader("Authorization") String token, @RequestBody Note note) {
        try {
            Long medecinId = jwtService.extractIdFromBearer(token);
            medecinService.addPrivateNote(medecinId, note);
            return ResponseEntity.ok("Note added successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get all private notes for a specific medecin (only the medecin who created them can see them)
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("me/notes")
    public ResponseEntity<List<Note>> getPrivateNotes(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<Note> notes = medecinService.getPrivateNotes(medecinId);
        return ResponseEntity.ok(notes);
    }

    // Delete a private note (only the medecin who created it can delete it)
    @PreAuthorize("hasRole('MEDECIN')")
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<String> deletePrivateNote(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
        try {
            Long medecinId = jwtService.extractIdFromBearer(token);
            // Delete the note by its ID, ensuring that it belongs to the requesting medecin
            medecinService.deletePrivateNote(medecinId, noteId);
            return ResponseEntity.ok("Private note deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getPatientsByMedecin(@PathVariable Long medecinId, @RequestHeader("Authorization") String token) {
        try {
            List<Patient> patients = medecinService.getPatientsByMedecin(medecinId);
            return ResponseEntity.ok(patients);
        } catch (RuntimeException e) {
            // If the Medecin is not found, return 404
            if (e.getMessage().equals("Doctor not found")) {
                return ResponseEntity.status(404).body(null);
            }
            // Handle other unexpected errors
            return ResponseEntity.status(500).body(null);
        }
    }

    // Medecin Block a patient (soft Block)
    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/patients/{patientId}/block")
    public ResponseEntity<String> blockPatient(@PathVariable Long patientId, @RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        try {

            Long medecinId = jwtService.extractIdFromBearer(token);
            String reason = request.get("reason");
            String description = request.get("description");

            User targetUser = userService.findById(patientId); // throws exception if not found
            if (!targetUser.getRole().equals(UserRole.Patient)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only patients can be blocked by a médecin.");
            }

            userService.blockUser(patientId, medecinId, reason, description);

            return ResponseEntity.status(HttpStatus.OK).body("Patient blocked by medecin successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    

}
