package iset.pfe.mediconnectback.entities;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import iset.pfe.mediconnectback.enums.DocumentVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DocumentMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String fichier;

    @CreatedDate
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id")
    @JsonIgnore
    private DossierMedical dossierMedical;

    // don't get the uploader password
    @ManyToOne(optional = true)
    @JoinColumn(name = "uploader_id", referencedColumnName = "id")
    private User uploader; //can be a medecin or a patient

    @Enumerated(EnumType.STRING)
    private DocumentVisibility visibility;

    @ManyToMany // one doc can be visible by many medecins, and one medecin can see many docs
    @JoinTable(
        name = "document_visible_medecins",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "medecin_id")
    )
    private List<Medecin> allowedMedecins; // Only for PRIVATE docs
}
