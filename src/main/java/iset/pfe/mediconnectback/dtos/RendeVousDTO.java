package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import iset.pfe.mediconnectback.enums.RendezVousStatut;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RendeVousDTO {
    private Long id;
    private LocalTime appointmentTime;
    private LocalDate appointmentDate;
    private String reason;
     private RendezVousStatut  rendezVousStatut;
     private LocalDateTime createdAt;
    private PatientResponse patient;
    private MedecinResponse medecin;
    // Getters & Setters
}



