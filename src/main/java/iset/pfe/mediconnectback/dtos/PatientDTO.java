package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import iset.pfe.mediconnectback.enums.UserStatus;
import lombok.Data;

@Data
public class PatientDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private Integer phoneNumber;
    private LocalDate dateNaissance;
    private String sexe;
    private String imageUrl;
    private UserStatus userStatus;
    private Long blockedByMedecinId;
    private LocalDateTime rendezVousCreatedDate;
    private DossierMedicalDTO dossierMedical;
}
