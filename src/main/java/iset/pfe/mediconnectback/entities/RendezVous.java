package iset.pfe.mediconnectback.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.enums.RendezVousType;
import jakarta.persistence.*;
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
    private LocalTime appointmentTime;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull

    private String reason; // Motif de la consultation

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @Enumerated(EnumType.STRING)
    private RendezVousStatut  rendezVousStatut;

    @Enumerated(EnumType.STRING)
    private RendezVousType rendezVousType; // Pour suivre l'historique des statuts

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    private LocalDateTime cancelledAt; // Date d'annulation de la consultation

    private LocalDateTime completedAt; // Date de fin de la consultation

    // Needed for RESCHEDULE_REQUESTED tracking
    private LocalDateTime rescheduleRequestTime;

    // Track how many times this appointment has been rescheduled
    private int rescheduleCount;

    // Computed field: date + time for scheduling logic
    public LocalDateTime getAppointmentDateTime() {
        return LocalDateTime.of(appointmentDate, appointmentTime);
    }

    private String errorMessage;    

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.rendezVousStatut = RendezVousStatut.Pending;
        this.rescheduleCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.rendezVousStatut == RendezVousStatut.Reschedule_Requested) {
            this.rescheduleRequestTime = LocalDateTime.now();
        }
        if (this.rendezVousStatut == RendezVousStatut.Confirmed) {
            this.completedAt = LocalDateTime.now();
        }
        if (this.rendezVousStatut == RendezVousStatut.Cancelled) {
            this.cancelledAt = LocalDateTime.now();
        }
        if (this.rendezVousStatut == RendezVousStatut.Rescheduled) {
            this.rescheduleCount++;
        }
    }

}
