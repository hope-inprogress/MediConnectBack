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
    private String specialite;
    private String workPlace;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    
}
