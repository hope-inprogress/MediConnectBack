package iset.pfe.mediconnectback.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.services.RendezVousService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/rendezvous")
public class RendezVousController {
    
    @Autowired
    private RendezVousService rendezVousService;

    @GetMapping
    public List<RendezVous> getAllRendezVous() {
        return rendezVousService.getAllRendezVous();
    }

      @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getRendezVousStats() {
        Map<String, Long> stats = rendezVousService.getRendezVousStats();
        return ResponseEntity.ok(stats);
    }

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
    
}
