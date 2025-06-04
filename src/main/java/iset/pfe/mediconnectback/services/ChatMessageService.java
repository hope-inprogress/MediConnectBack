package iset.pfe.mediconnectback.services;

import iset.pfe.mediconnectback.dtos.ChatMessage;
import iset.pfe.mediconnectback.dtos.ConversationDTO;
import iset.pfe.mediconnectback.dtos.MessageDTO;
import iset.pfe.mediconnectback.dtos.UserSummaryDTO;
import iset.pfe.mediconnectback.entities.Conversation;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Message;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.MessageType;
import iset.pfe.mediconnectback.repositories.ConversationRepository;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.repositories.MessageRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import iset.pfe.mediconnectback.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired 
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    // ✅ Send a message and return MessageDTO
    @Transactional
    public MessageDTO sendMessage(ChatMessage chatMessage, Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(chatMessage.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Conversation conversation = findOrCreatePrivateConversation(sender.getId(), receiver.getId());

        Message message = new Message();
        message.setSender(sender);
        message.setContent(chatMessage.getContent());
        message.setConversation(conversation);
        message.setDateCreated(LocalDateTime.now());
        message.setDateSent(LocalDateTime.now());
        message.setIsRead(false);
        message.setIsDeleted(false);
        message.setType(chatMessage.getType() != null ? chatMessage.getType() : MessageType.TEXT);

        conversation.setLastUpdated(LocalDateTime.now());
        conversationRepository.save(conversation);
        messageRepository.save(message);

        return mapToDTO(message);
    }
    

    @Transactional
    // ✅ Return all messages in a conversation as DTOs
    public List<MessageDTO> getMessagesByConversationId(Long conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByDateCreatedAsc(conversationId);
        return messages.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    // ✅ Search messages in a conversation as DTOs
    public List<MessageDTO> searchMessagesInConversation(Long conversationId, String keyword) {
        List<Message> messages = messageRepository.findByConversationIdAndContentContainingIgnoreCase(conversationId, keyword);
        return messages.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    // ✅ Soft delete all messages in a conversation
    public void deleteConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversationRepository.delete(conversation);
    }

    @Transactional              
    // ✅ Delete selected conversations
    public void deleteConversations(List<Long> conversationIds) {
        conversationRepository.deleteAllById(conversationIds);
    }

    // a medecin search for his patient by name
    @Transactional
    public List<UserSummaryDTO> searchPatientsByName(Long userId, String name) {
        List<Patient> conversations = patientRepository.searchForAPatient(userId, name);
        return conversations.stream().map(this::mapToUserSummaryDTO).collect(Collectors.toList());
        
    }

    // a patient search for his medecin by his name or speciality
    @Transactional
    public List<UserSummaryDTO> searchMedecinsByNameOrSpecialite(Long userId, String name) {
        List<Medecin> medecins = medecinRepository.searchForAMedecin( name, userId);
        return medecins.stream().map(this::mapToUserSummaryDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<ConversationDTO> getAllUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findAllByParticipantsId(userId);
        return conversations.stream().map(this::mapToCDTO).collect(Collectors.toList());
    }

    private UserSummaryDTO mapToUserSummaryDTO(Patient patient) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(patient.getId());
        dto.setFullName(patient.getFullName());
        return dto;
    }

    private UserSummaryDTO mapToUserSummaryDTO(Medecin medecin) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(medecin.getId());
        dto.setFullName(medecin.getFullName());
        dto.setSpecialite(medecin.getSpecialitePrimaire().toString());
        return dto;
    }

    private ConversationDTO mapToCDTO(Conversation conv) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conv.getId());
        dto.setType(conv.getType());
        dto.setCreatedAt(conv.getCreatedAt());
        dto.setLastUpdated(conv.getLastUpdated());
    
        List<UserSummaryDTO> participantDTOs = conv.getParticipants().stream().map(user -> {
            UserSummaryDTO u = new UserSummaryDTO();
            u.setId(user.getId());
            u.setFullName(user.getFullName());
            if (user instanceof Medecin medecin) {
                u.setSpecialite(medecin.getSpecialitePrimaire().toString());
            }
            return u;
        }).toList();

    
        dto.setParticipants(participantDTOs);
        return dto;
    }
    
    @Transactional
    // ✅ Find or create conversation (unchanged)
    public Conversation findOrCreatePrivateConversation(Long userId, Long otherUserId) {
        User user1 = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));

        return conversationRepository.findPrivateConversationBetween(user1.getId(), user2.getId())
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setType("PRIVATE");
                    newConversation.setCreatedAt(LocalDateTime.now());
                    newConversation.setLastUpdated(LocalDateTime.now());
                    newConversation.setParticipants(new ArrayList<>(List.of(user1, user2)));
                    return conversationRepository.save(newConversation);
                });
    }

    @Transactional
    // ✅ Convert Message entity to DTO
    private MessageDTO mapToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setType(message.getType());
        dto.setDateCreated(message.getDateCreated());
        dto.setDateSent(message.getDateSent());
        dto.setDateReceived(message.getDateReceived());

        User sender = message.getSender();
        UserSummaryDTO senderDTO = new UserSummaryDTO();
        senderDTO.setId(sender.getId());
        senderDTO.setFullName(sender.getFullName());
        dto.setSender(senderDTO);

        return dto;
    }
}
