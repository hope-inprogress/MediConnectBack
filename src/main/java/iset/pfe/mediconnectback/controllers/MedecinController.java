package iset.pfe.mediconnectback.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.MedecinService;
import iset.pfe.mediconnectback.services.UserService;


@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/medecins")
public class MedecinController {
    
    @Autowired
    private MedecinService medecinService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Medecin> getMedecins() {
        return medecinService.getAllMedecins();
 
    }

    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/patients/{patientId}/block")
    public ResponseEntity<String> blockPatient(@PathVariable Long patientId, @RequestBody Map<String, String> request) {
        try {

            Long medecinId = jwtService.extractIdFromBearer(request.get("token"));
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
