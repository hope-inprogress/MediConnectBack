package iset.pfe.mediconnectback.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.services.JwtService;
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

       // Medecin Block a patient (soft Block)
    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/{patientId}/block")
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
