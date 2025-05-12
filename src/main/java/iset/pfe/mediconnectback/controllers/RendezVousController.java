package iset.pfe.mediconnectback.controllers;

import java.util.Arrays;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.dtos.StatusUpdateRequestDTO;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.MedecinService;
import iset.pfe.mediconnectback.services.RendezVousService;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/rendezvous")
public class RendezVousController {
    
    @Autowired
    private RendezVousService rendezVousService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MedecinService medecinService;
    

    @GetMapping
    public List<RendezVous> getAllRendezVous() {
        return rendezVousService.getAllRendezVous();
    }

    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateRendezvousStatus(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        try {
            // Extract the status from the request body
            String newStatusStr = requestBody.get("rendezStatut");
            
            // Ensure the status is provided
            if (newStatusStr == null) {
                return ResponseEntity.badRequest().body("Error: 'rendezStatut' is required in the request body");
            }
    
            // Try to convert the string to the enum
            RendezVousStatut newStatus;
            try {
                newStatus = RendezVousStatut.valueOf(newStatusStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Error: Invalid 'rendezStatut'. Valid values are: " + Arrays.toString(RendezVousStatut.values()));
            }
    
            // Call the service to update the rendezvous status
            RendezVous updatedRendezvous = rendezVousService.updateRendezvousStatus(id, newStatus);
            
            // Handle not found or no content
            if (updatedRendezvous == null) {
                return ResponseEntity.status(404).body("Rendezvous not found or status not updated");
            }
    
            // Return the updated rendezvous entity
            return ResponseEntity.ok(updatedRendezvous);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    
    // Update the status of an appointment (Only the assigned medecin can update)
    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/{rendezVousId}/status")
    public ResponseEntity<String> updateAppointmentStatus(@RequestHeader("Authorization") String token, @PathVariable Long rendezVousId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            Long medecinId = jwtService.extractIdFromBearer(token);
            medecinService.updateAppointmentStatus(rendezVousId, status, medecinId);
            return ResponseEntity.ok("Appointment status updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/update-status")
public ResponseEntity<RendezVous> updateStatusManually(
        @PathVariable("id") Long appointmentId,
        @RequestBody @Valid StatusUpdateRequestDTO dto) {

    RendezVous updated = rendezVousService.updateStatusManually(
            appointmentId,
            dto.getNewStatus(),
            dto.getErrorMessage(),
            dto.getDoctorId()
    );

    return ResponseEntity.ok(updated);
}
    
}
