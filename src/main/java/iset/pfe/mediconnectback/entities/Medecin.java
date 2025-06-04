package iset.pfe.mediconnectback.entities;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import iset.pfe.mediconnectback.enums.RendezVousType;
import iset.pfe.mediconnectback.enums.Specialite;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.UniqueConstraint;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"appointments", "notes", "documentsMedicaux", "mesPatients", "blockedPatients"})
public class Medecin extends User {

	@Column(unique = true, nullable = false)
	private String codeMedical;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private Specialite specialitePrimaire;

	@ElementCollection(targetClass = Specialite.class)
	@Enumerated(EnumType.STRING)
	private List<Specialite> specialiteSecondaire = new ArrayList<>();

	private String workPlace;

	@ElementCollection
	@Column(name = "working_day")
	private Set<DayOfWeek> workingDays = new HashSet<>();

	
	@ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private List<Conversation> conversations = new ArrayList<>();

	private LocalTime startTime; // format HH:mm:ss

	private LocalTime endTime; // format HH:mm:ss

	@OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<MedecinHoliday> holidays = new ArrayList<>();

	private Boolean isAvailable;

	private boolean autoManageAppointments; // true = auto, false = manual

	@Enumerated(EnumType.STRING)
	private RendezVousType rendezVousType; // EnLigne or EnPersonne

	private Long priceOnline; // Price for online appointments
	private Long priceInPerson; // Price for in-person appointments


	private String description;

	@JsonIgnore
	@OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RendezVous> appointments = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<Note> notes = new ArrayList<>();

	@OneToMany(mappedBy = "uploader")  // One Patient can upload many documents
	@JsonIgnore // Prevent serialization
	private List<DocumentMedical> documentsMedicaux = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "medecin_mes_patients",
		joinColumns = @JoinColumn(name = "medecin_id", referencedColumnName = "id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
	)
	@JsonIgnoreProperties({"mesMedecins", "favoriteMedecins", "appointments", "notes", "documentsMedicaux", "dossierMedical"})
	private Set<Patient> mesPatients = new HashSet<>();

	@OneToMany(mappedBy = "blockedBy")
    @JsonIgnore
    private Set<Patient> blockedPatients = new HashSet<>();

	@PreRemove
	private void removePatientLinks() {
		if (mesPatients != null) {
			for (Patient patient : mesPatients) {
				patient.getMesMedecins().remove(this);
			}
			mesPatients.clear();
		}
	}

}
