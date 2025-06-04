package iset.pfe.mediconnectback.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "patient")
@Data
public class DossierMedical {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @Column(name = "created_at")
    private LocalDateTime dateCreated;

    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Prevent serialization
    private List<DocumentMedical> fichiers;

    @OneToOne(mappedBy = "dossierMedical", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private FichierMedicalForm medicalForm;
}
