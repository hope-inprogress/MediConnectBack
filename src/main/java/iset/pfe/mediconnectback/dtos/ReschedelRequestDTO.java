package iset.pfe.mediconnectback.dtos;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReschedelRequestDTO {
    private LocalDateTime date;
    private String rendeVousType;
    private String reason;
}
