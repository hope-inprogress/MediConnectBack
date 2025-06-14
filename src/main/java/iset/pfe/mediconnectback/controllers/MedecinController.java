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

import iset.pfe.mediconnectback.dtos.PatientDTO;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.dtos.WorkDaysDTO;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Note;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.MedecinService;
import iset.pfe.mediconnectback.services.NoteService;
import iset.pfe.mediconnectback.services.PatientService;
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
    private MedecinRepository medecinRepository;

    @Autowired
    private PatientService patientService;

    @Autowired
    private NoteService noteService;

    // Get all medecins (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Medecin> getAllMedecins() {
        return medecinService.getAllMedecins();
    }

    // Get all patients for a specific medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/me/patients")
    public ResponseEntity<List<PatientDTO>> getPatientsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<PatientDTO> patientDTOs = patientService.getPatientsByMedecin(medecinId);
        return ResponseEntity.ok(patientDTOs);
    }

    // Get Latest 5 patients for a specific medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/me/patients/latest")
    public ResponseEntity<List<PatientDTO>> getLatestPatientsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<PatientDTO> latestPatients = medecinService.getLatestPatientsByMedecin(medecinId);
        return ResponseEntity.ok(latestPatients);
    }

    // Get all appointments (RendezVous) for a specific medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/me/appointments")
    public ResponseEntity<List<RendeVousDTO>> getAppointmentsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<RendeVousDTO> appointments = medecinService.getAppointmentsByMedecin(medecinId);
        return ResponseEntity.ok(appointments);
    }

    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/me/appointments/next")
    public ResponseEntity<List<RendeVousDTO>> getNextAppointments(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<RendeVousDTO> nextAppointment = medecinService.getNextAppointmentsByMedecin(medecinId);
        return ResponseEntity.ok(nextAppointment);
    }

    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/auto-manage")
    public ResponseEntity<?> updateAutoManageAppointments(
        @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {
                Long medecinId = jwtService.extractIdFromBearer(token);
        // Extract autoManageAppointments from body
        Object value = body.get("autoManageAppointments");
        if (!(value instanceof Boolean)) {
            return ResponseEntity.badRequest().body("autoManageAppointments must be a boolean");
        }
    
        // Update the field directly in the database
        int updatedRows = medecinRepository.updateAutoManageAppointments(medecinId, (Boolean) value);
        if (updatedRows == 0) {
            return ResponseEntity.notFound().build();
        }
    
        // Fetch the updated Medecin to return
        Medecin updatedMedecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        return ResponseEntity.ok(updatedMedecin);
    }

    // Add a private note for a medecin (only the medecin can add notes to their profile)
    @PreAuthorize("hasRole('MEDECIN')")
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
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("me/notes")
    public ResponseEntity<List<Note>> getPrivateNotes(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<Note> notes = noteService.getPrivateNotes(medecinId);
        return ResponseEntity.ok(notes);
    }

        // Update a private note (only the medecin who created it can update it)
    @PreAuthorize("hasRole('MEDECIN')")
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
    @PreAuthorize("hasRole('MEDECIN')")
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
    
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/getautomanage")
    public ResponseEntity<Boolean> getAutoManageAppointments(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        Medecin medecin = medecinService.getMedecinById(medecinId);
        return ResponseEntity.ok(medecin.isAutoManageAppointments());
    }
    
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/patients/{patientId}/add")
    public ResponseEntity<String> addPatientToMedecin(
            @PathVariable Long patientId, 
            @RequestHeader("Authorization") String token) {
        try {
            Long medecinId = jwtService.extractIdFromBearer(token);
            medecinService.addToPatient(patientId, medecinId);
            return ResponseEntity.ok("Patient added successfully to doctor's list");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('MEDECIN')")
    @DeleteMapping("/patients/{patientId}/remove")
    public ResponseEntity<String> removePatientFromMedecin(
            @PathVariable Long patientId,
            @RequestHeader("Authorization") String token) {
        try {
            Long medecinId = jwtService.extractIdFromBearer(token);
            medecinService.removePatientFromMedecin(medecinId, patientId);
            return ResponseEntity.ok("Patient removed successfully from doctor's list");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
