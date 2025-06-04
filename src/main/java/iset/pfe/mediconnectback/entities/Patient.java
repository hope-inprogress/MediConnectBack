package iset.pfe.mediconnectback.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"dossierMedical", "appointments", "notes", "documentsMedicaux", "mesMedecins"})
@AllArgsConstructor
@NoArgsConstructor
public class Patient extends User {


    @JsonIgnore
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RendezVous> appointments = new HashSet<>();

    @Column(name = "blocked_by_medecin_ids")
    private String blockedByMedecinIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_medecin_id")
    @JsonIgnore
    private Medecin blockedBy;

    
    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private List<Conversation> conversations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Note> notes = new ArrayList<>();

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevent serialization
    private DossierMedical dossierMedical;

    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL, orphanRemoval = true)  // One Patient can upload many documents
    @JsonIgnore // Prevent serialization
    private List<DocumentMedical> documentsMedicaux;

    @ManyToMany(mappedBy = "mesPatients", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties({"mesPatients", "favoriteMedecinList", "appointments", "notes", "documentsMedicaux", "dossierMedical"})
    private Set<Medecin> mesMedecins = new HashSet<>();

    // ✅ New field: favoriteMedecins
    @ManyToMany
    @JoinTable(
        name = "favorite_medecins",
        joinColumns = @JoinColumn(name = "patient_id"),
        inverseJoinColumns = @JoinColumn(name = "medecin_id")
    )
    @JsonIgnoreProperties({
        "mesPatients", "appointments", "notes", "documentsMedicaux", "dossierMedical"
    })
    private Set<Medecin> favoriteMedecins = new HashSet<>();

    @PreRemove
    private void removeMedecinLinks() {
        if (mesMedecins != null) {
            mesMedecins.forEach(m -> m.getMesPatients().remove(this));
            mesMedecins.clear();
        }
    
        if (favoriteMedecins != null) {
            favoriteMedecins.clear(); // Just clear from this side
        }
    }
    
}
