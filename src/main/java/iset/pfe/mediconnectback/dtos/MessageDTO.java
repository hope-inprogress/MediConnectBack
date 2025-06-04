package iset.pfe.mediconnectback.dtos;

import java.time.LocalDateTime;

import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.MessageType;
import lombok.Data;

@Data
public class MessageDTO {
    private Long id;
    private String content;
    private MessageType type; // TEXT or DOCUMENT
    private LocalDateTime dateCreated;
    private LocalDateTime dateSent;
    private LocalDateTime dateReceived;
    private UserSummaryDTO sender;
    private String fileName;
    private String fileType;
    private String fileDataBase64;
}
