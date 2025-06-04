package iset.pfe.mediconnectback.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RendezVousRequestDTO {
    private Long medecinId;
    private LocalDateTime date;
    private String rendeVousType;
    private String reason;
}
