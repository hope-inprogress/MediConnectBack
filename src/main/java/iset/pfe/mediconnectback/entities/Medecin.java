package iset.pfe.mediconnectback.entities;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
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

	@Column(unique = true, nullable = true)
	private String codeMedical;

	private String specialite;

	private String workPlace;

	private LocalTime startTime; // format HH:mm:ss

	private LocalTime endTime; // format HH:mm:ss

	private Boolean isAvailable;

	private boolean autoManageAppointments; // true = auto, false = manual

	private Long  startingPrice; // prix de consultation,

	private String description;

	@JsonIgnore
	@OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RendezVous> appointments = new HashSet<>();

	@OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Note> notes = new ArrayList<>();

	@OneToMany(mappedBy = "uploader")  // One Patient can upload many documents
	@JsonIgnore // Prevent serialization
    private List<DocumentMedical> documentsMedicaux;

	@ManyToMany(mappedBy = "mesMedecins", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnore // Prevent serialization
	private List<Patient> mesPatients; // Many Medecins can have many Patients

	@OneToMany(mappedBy = "blockedBy")
    @JsonIgnore
    private Set<Patient> blockedPatients = new HashSet<>();

}
