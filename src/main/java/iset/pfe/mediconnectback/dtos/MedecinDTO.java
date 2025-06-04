package iset.pfe.mediconnectback.dtos;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import iset.pfe.mediconnectback.enums.RendezVousType;
import iset.pfe.mediconnectback.enums.UserStatus;
import lombok.Data;

@Data
public class MedecinDTO {
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
    private String codeMedical;
    private String typeRendezVous;
    private String specialitePrimaire;
    private List<String> specialiteSecondaire;
    private Set<String> workDays;
    private String startTime;
    private String endTime;
    private Boolean isAvailable;
    private String workPlace;
    private String description;
    private Long priceOnline;
    private Long priceInPerson;
  
    


}
