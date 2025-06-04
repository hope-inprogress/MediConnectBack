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

import iset.pfe.mediconnectback.dtos.MedecinDTO;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.entities.Note;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.MedecinService;
import iset.pfe.mediconnectback.services.NoteService;
import iset.pfe.mediconnectback.services.PatientService;
import iset.pfe.mediconnectback.services.UserService;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:5173")
public class PatientController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private MedecinService medecinService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private NoteService noteService;

    // Get all patients for a specific medecin
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/medecins")
    public ResponseEntity<List<MedecinDTO>> getMedecinsByPatient(@RequestHeader("Authorization") String token) {
        Long userId = jwtService.extractIdFromBearer(token);
        List<MedecinDTO> medecinDTOs = medecinService.getMedecinsByPatient(userId);
        return ResponseEntity.ok(medecinDTOs);
    }

        // Get all appointments (RendezVous) for a specific medecin
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/appointments")
    public ResponseEntity<List<RendeVousDTO>> getAppointmentsByPatient(@RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        List<RendeVousDTO> appointments = patientService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/appointments/next")
    public ResponseEntity<List<RendeVousDTO>> getNextAppointments(@RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        List<RendeVousDTO> nextAppointment = patientService.getNextAppointmentsByPatient(patientId);
        return ResponseEntity.ok(nextAppointment);
    }

    // Add a private note for a medecin (only the medecin can add notes to their profile)
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/notes")
    public ResponseEntity<String> addPrivateNote(@RequestHeader("Authorization") String token, @RequestBody Note note) {
        try {
            Long medecinId = jwtService.extractIdFromBearer(token);
            noteService.addPrivateNote(medecinId, note);
            return ResponseEntity.ok("Note added successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get all private notes for a specific medecin (only the medecin who created them can see them)
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("me/notes")
    public ResponseEntity<List<Note>> getPrivateNotes(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<Note> notes = noteService.getPrivateNotes(medecinId);
        return ResponseEntity.ok(notes);
    }

        // Update a private note (only the medecin who created it can update it)
    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/update/{noteId}")
    public ResponseEntity<String> updatePrivateNote(
            @RequestHeader("Authorization") String token,
            @PathVariable Long noteId,
            @RequestBody Note updatedNote) {
        try {
            Long medecinId = jwtService.extractIdFromBearer(token);
            noteService.updateNote(noteId, medecinId, updatedNote); // Corrected param order
            return ResponseEntity.ok("Private note updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + e.getMessage());
        }
    }

    // Delete a private note (only the medecin who created it can delete it)
    @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<String> deletePrivateNote(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
        try {
            Long medecinId = jwtService.extractIdFromBearer(token);
            // Delete the note by its ID, ensuring that it belongs to the requesting medecin
            noteService.deletePrivateNote(medecinId, noteId);
            return ResponseEntity.ok("Private note deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/addToFavorites/{medecinId}")
    public ResponseEntity<?> addToFavorites(@PathVariable Long medecinId,
                                            @RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        patientService.addFavoriteMedecin(patientId, medecinId);
        return ResponseEntity.ok("Added to favorites successfully.");
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/favorites")
    public ResponseEntity<List<MedecinDTO>> getFavoriteMedecins(@RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        List<MedecinDTO> favoriteMedecins = patientService.getFavoriteMedecins(patientId);
        return ResponseEntity.ok(favoriteMedecins);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("/me/favorites/{medecinId}")
    public ResponseEntity<String> removeFromFavorites(@PathVariable Long medecinId,
                                                    @RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        patientService.removeFromFavorites(patientId, medecinId);
        return ResponseEntity.ok("Removed from favorites successfuly.");
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/me/addToMedecin/{medecinId}")
    public ResponseEntity<String> addToMedecin(@PathVariable Long medecinId,
                                                @RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        medecinService.addToPatient(patientId, medecinId);
        return ResponseEntity.ok("Medecin added successfully.");
    }

        @PreAuthorize("hasRole('PATIENT')")
    @DeleteMapping("/me/removeFromMedecin/{medecinId}")
    public ResponseEntity<String> removeFromMedecin(@PathVariable Long medecinId,
                                                @RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        medecinService.removePatientFromMedecin(medecinId, patientId);
        return ResponseEntity.ok("Medecin removed successfully.");
    }


}
