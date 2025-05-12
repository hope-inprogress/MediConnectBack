package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;

import iset.pfe.mediconnectback.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MotifDto {

    private Long id;
    private EventType eventType;
    private LocalDate eventTime;
    private String reason;
    private String description;
    private Long targetUserId;
    private Long performedById;

}
