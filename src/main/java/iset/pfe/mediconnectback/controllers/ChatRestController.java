package iset.pfe.mediconnectback.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import iset.pfe.mediconnectback.dtos.ChatMessage;
import iset.pfe.mediconnectback.dtos.ConversationDTO;
import iset.pfe.mediconnectback.dtos.MessageDTO;
import iset.pfe.mediconnectback.dtos.UserSummaryDTO;
import iset.pfe.mediconnectback.entities.Conversation;
import iset.pfe.mediconnectback.entities.Message;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.services.ChatMessageService;
import iset.pfe.mediconnectback.services.JwtService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("http://localhost:5173")
public class ChatRestController {

    @Autowired
    private ChatMessageService chatService;

    @Autowired
    private JwtService jwtService;

    // Send a message
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody ChatMessage dto,
                                               @RequestHeader("Authorization") String token) {
        Long senderId = jwtService.extractIdFromBearer(token);
        MessageDTO message = chatService.sendMessage(dto, senderId);
        return ResponseEntity.ok(message);
    }

    // Get all messages in a conversation
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getMessagesInConversation(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getMessagesByConversationId(conversationId));
    }

    // Get all conversations for user
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@RequestHeader("Authorization") String token) {
        Long userId = jwtService.extractIdFromBearer(token);
        return ResponseEntity.ok(chatService.getAllUserConversations(userId));
    }

    // Delete all messages in a conversation
    @DeleteMapping("/conversation/{conversationId}/delete")
    public ResponseEntity<Void> deleteConversationMessages(@PathVariable Long conversationId) {
        chatService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }

    // Delete selected conversations
    @DeleteMapping("/conversations/delete")
    public ResponseEntity<Void> deleteConversations(@RequestBody List<Long> conversationIds) {
        chatService.deleteConversations(conversationIds);
        return ResponseEntity.noContent().build();
    }

    // Search messages in a conversation
    @GetMapping("/conversation/{conversationId}/search")
    public ResponseEntity<List<MessageDTO>> searchMessages(@PathVariable Long conversationId,
                                                        @RequestParam String keyword) {
        return ResponseEntity.ok(chatService.searchMessagesInConversation(conversationId, keyword));
    }

    
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/search-Patients")
    public ResponseEntity<List<UserSummaryDTO>> searchPatientsByName(@RequestHeader("Authorization") String token,
                                                                         @RequestParam String name) {
        Long userId = jwtService.extractIdFromBearer(token);
        return ResponseEntity.ok(chatService.searchPatientsByName(userId, name));
    }

    
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/search-Medecins")
    public ResponseEntity<List<UserSummaryDTO>> searchMedecinsByNameOrSpecialite(@RequestHeader("Authorization") String token,
                                                                                  @RequestParam String name) {
        Long userId = jwtService.extractIdFromBearer(token);
        return ResponseEntity.ok(chatService.searchMedecinsByNameOrSpecialite(userId, name));
    }
}
