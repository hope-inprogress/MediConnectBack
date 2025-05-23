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

import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.dtos.StatusUpdateRequestDTO;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.MedecinService;
import iset.pfe.mediconnectback.services.RendezVousService;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/rendezvous")
public class RendezVousController {
    
    @Autowired
    private RendezVousService rendezVousService;

    @Autowired
    private JwtService jwtService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<RendeVousDTO> getAllRendezVous() {
        return rendezVousService.getAllRendezVous();
    }

        @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/{id}/u-status")
    public ResponseEntity<RendezVous> updateStatusManually(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long appointmentId,
            @RequestBody @Valid StatusUpdateRequestDTO dto) {
        
        // Extract doctor ID from token instead of DTO
        Long doctorId = jwtService.extractIdFromBearer(token);
        
        RendezVous updated = rendezVousService.updateStatusManually(
                appointmentId,
                dto.getNewStatus(),
                dto.getErrorMessage(),
                doctorId // Use the ID from token
        );
        
        return ResponseEntity.ok(updated);
    }
    
    
}
