package iset.pfe.mediconnectback.entities;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Medecin extends User {

	private String codeMedical;

	private String specialite;

	private String workPlace;

	private LocalTime startTime; // format HH:mm:ss

	private LocalTime endTime; // format HH:mm:ss

	private Boolean isAvailable;

	private Number startinPrice;

	private String description;

	@JsonIgnore
	@OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RendezVous> appointments = new HashSet<>();

	@OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Note> notes = new ArrayList<>();

    
    @OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Prevent serialization
    private List<DossierMedical> dossiersMedicaux = new ArrayList<>();
}
