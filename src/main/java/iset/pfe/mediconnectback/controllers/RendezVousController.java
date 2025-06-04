package iset.pfe.mediconnectback.controllers;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.dtos.LocalDateTimeDTO;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.dtos.RendezVousRequestDTO;
import iset.pfe.mediconnectback.dtos.ReschedelRequestDTO;
import iset.pfe.mediconnectback.dtos.StatusUpdateRequestDTO;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.services.JwtService;
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
    
    // Book an appointement 
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/book")
    public ResponseEntity<RendeVousDTO> bookAppointment(
        @RequestHeader("Authorization") String token,
        @RequestBody @Valid RendezVousRequestDTO dto) {
        Long patientId = jwtService.extractIdFromBearer(token);
        RendeVousDTO rendezVous = rendezVousService.bookAppointment(
            patientId, 
            dto
        );
        return ResponseEntity.ok(rendezVous);
    }

// http://localhost:8080/api/rendezvous/availableDateTimeslots/{medecinId}?month=2025-06
@PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/availableDateTimeslots/{medecinId}")
    public ResponseEntity<List<LocalDateTimeDTO>> getAvailableDateTimeslots(
        @PathVariable Long medecinId,
        @RequestParam String month // e.g., "2025-06"
    ) {
        List<LocalDateTimeDTO> availableDateTimeslots = rendezVousService.getAvailableDateTimeslots(medecinId, month);
        return ResponseEntity.ok(availableDateTimeslots);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<RendeVousDTO> cancelAppointment(
        @RequestHeader("Authorization") String token,
        @PathVariable Long appointmentId) { 
        Long patientId = jwtService.extractIdFromBearer(token);
        RendeVousDTO cancelledAppointment = rendezVousService.cancelAppointment(patientId, appointmentId);
        return ResponseEntity.ok(cancelledAppointment);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/rescheduleRequest/{appointmentId}")
    public ResponseEntity<RendeVousDTO> rescheduleAppointment(
        @RequestHeader("Authorization") String token,
        @PathVariable Long appointmentId,
        @RequestBody @Valid ReschedelRequestDTO dto) {
        Long patientId = jwtService.extractIdFromBearer(token);
        RendeVousDTO rescheduledAppointment = rendezVousService.rescheduleRequestAppointment(
            patientId,
            appointmentId,
            dto
        );
        return ResponseEntity.ok(rescheduledAppointment);
    }


}
