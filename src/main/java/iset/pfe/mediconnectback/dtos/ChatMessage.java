package iset.pfe.mediconnectback.dtos;

import iset.pfe.mediconnectback.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long receiverId;
    private String content;
    private MessageType type; 
    private String fileDataBase64;
    private String fileName;
}
