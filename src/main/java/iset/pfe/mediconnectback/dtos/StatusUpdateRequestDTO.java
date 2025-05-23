package iset.pfe.mediconnectback.dtos;

import iset.pfe.mediconnectback.enums.RendezVousStatut;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequestDTO {

    @NotNull
    private RendezVousStatut newStatus;

    private String errorMessage;


}
