package iset.pfe.mediconnectback.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;

import iset.pfe.mediconnectback.dtos.ChatMessage;
import iset.pfe.mediconnectback.dtos.MessageDTO;
import iset.pfe.mediconnectback.entities.Message;
import iset.pfe.mediconnectback.enums.MessageType;
import iset.pfe.mediconnectback.services.ChatMessageService;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserService;

@Controller
@CrossOrigin("http://localhost:5173")
public class ChatWebSocketController {

    @Autowired
    private ChatMessageService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @MessageMapping("/send")
    public void handleMessage(@Payload ChatMessage dto, Principal principal) {
        if (principal == null || !(principal instanceof Authentication)) {
            throw new RuntimeException("Unauthorized WebSocket user");
        }

        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();
        String username = userDetails.getUsername();
        Long senderId = userService.findByEmail(username).get().getId();

        MessageDTO savedMessage = chatService.sendMessage(dto, senderId);

        // Get receiver's email from the original ChatMessage
        String receiverEmail = userService.findById(dto.getReceiverId()).getEmail();

        messagingTemplate.convertAndSendToUser(
            receiverEmail,
            "/queue/messages",
            savedMessage
        );
    }

    @MessageMapping("/send-document")
    public void handleDocumentMessage(@Payload ChatMessage dto, Principal principal) throws IOException {
        UserDetails userDetails = (UserDetails) ((Authentication) principal).getPrincipal();
        String username = userDetails.getUsername();
        Long senderId = userService.findByEmail(username).get().getId();

        if (dto.getType() == MessageType.DOCUMENT && dto.getFileDataBase64() != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(dto.getFileDataBase64());
            String fileName = UUID.randomUUID() + "_" + dto.getFileName();
            Path filePath = Paths.get("uploads").resolve(fileName);
            Files.write(filePath, decodedBytes);

            dto.setContent("/uploads/" + fileName); // Use chemin
        }

        MessageDTO saved = chatService.sendMessage(dto, senderId);

        // Get receiver's email from the original ChatMessage
        String receiverEmail = userService.findById(dto.getReceiverId()).getEmail();

        messagingTemplate.convertAndSendToUser(
            receiverEmail,
            "/queue/messages",
            saved
        );
    }
}

