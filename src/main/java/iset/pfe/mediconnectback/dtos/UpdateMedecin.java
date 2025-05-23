package iset.pfe.mediconnectback.dtos;

import java.time.LocalTime;

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
    private Long startingPrice; // prix de consultation
    private String description;

    
}
