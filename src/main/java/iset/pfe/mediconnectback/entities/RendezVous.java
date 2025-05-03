package iset.pfe.mediconnectback.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import iset.pfe.mediconnectback.enums.RendezVousStatut;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDateTime appointmentTime;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull

    private String reason; // Motif de la consultation

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    @NotNull
    private Medecin medecin;

    @Enumerated(EnumType.STRING)
    private RendezVousStatut  rendezVousStatut;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    private LocalDateTime cancelledAt; // Date d'annulation de la consultation

    private LocalDateTime completedAt; // Date de fin de la consultation



}
