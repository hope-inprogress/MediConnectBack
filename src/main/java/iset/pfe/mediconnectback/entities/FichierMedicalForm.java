package iset.pfe.mediconnectback.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FichierMedicalForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Health Info
    private String bloodType;
    private Double height; // in cm
    private Double weight; // in kg

    // Medical History
    @Column(length = 1000)
    private String allergies;

    @Column(length = 1000)
    private String chronicDiseases;

    @Column(length = 1000)
    private String currentMedications;

    @Column(length = 1000)
    private String surgicalHistory;

    @Column(length = 1000)
    private String familyMedicalHistory;

    // Optional: Lifestyle or other useful notes
    private Boolean smoker;
    private Boolean alcoholUse;
    private String activityLevel; // e.g. Sedentary, Moderate, Active
    private String dietaryPreferences;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierMedical dossierMedical;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
