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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMedecin extends UpdateUser {

    private String codeMedical;
    private String workPlace;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    private Boolean autoManageAppointments; // true = auto, false = manual
    private Set<DayOfWeek> workingDays;
    private List<MedecinHoliday> holidays; // List of holidays for the doctor
    private String typeRendezVous; // EnLigne or EnPersonne 
    private Long priceOnline;
    private Long priceInPerson; // Price for in-person appointments
    private String description;
    private Specialite specialitePrimaire;
    private List<Specialite> specialiteSecondaire;

    
}
