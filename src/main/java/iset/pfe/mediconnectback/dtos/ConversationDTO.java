package iset.pfe.mediconnectback.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ConversationDTO {
    private Long id;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<UserSummaryDTO> participants;
}
