package iset.pfe.mediconnectback.entities;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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

	@JsonIgnore
	@OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RendezVous> appointments = new HashSet<>();

}
