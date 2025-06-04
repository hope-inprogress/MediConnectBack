package iset.pfe.mediconnectback.dtos;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import iset.pfe.mediconnectback.entities.MedecinHoliday;
import iset.pfe.mediconnectback.enums.Specialite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MedecinResponse {
    
    private String firstName;
    private String lastName;
    private String sexe;
    private LocalDate dateNaissance;
    private String email;
    private String address;
    private String imageUrl;
    private String accountStatus;
    private String codeMedical;
    private Integer phoneNumber;
    private String workPlace;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    private Specialite specialitePrimaire;
    private List<Specialite> specialiteSecondaire;
    private String typeRendezVous; // the type of appointment
    private Boolean autoManageAppointments; // true = auto, false = manual
    private String Description;
    private Set<String> workDays;
    // get holidays
    private Set<String> holidays;
    private Long priceOnline;
    private Long priceInPerson;
    
    
}
