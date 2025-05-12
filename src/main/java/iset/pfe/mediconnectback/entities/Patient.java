package iset.pfe.mediconnectback.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Patient extends User {

    private Long CIN;

    @JsonIgnore
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RendezVous> appointments = new HashSet<>();

    @Column(name = "blocked_by_medecin_ids")
    private String blockedByMedecinIds;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonIgnore // Prevent serialization
    private DossierMedical dossierMedical;

    @OneToMany(mappedBy = "patient")  // One Patient can upload many documents
    @JsonIgnore // Prevent serialization
    private Set<DocumentMedical> documentsMedicaux;

    @ManyToMany(mappedBy = "patients", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore // Prevent serialization
    private Set<Medecin> mesMedecins = new HashSet<>(); // Many Patients can have many Medecins



}
