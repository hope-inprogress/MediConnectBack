package iset.pfe.mediconnectback.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import iset.pfe.mediconnectback.enums.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"conversations", "tokens", "authorities", "messages", "hibernateLazyInitializer", "handler"})
    private User sender;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnoreProperties({"messages", "participants"})
    private Conversation conversation;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private MessageType type = MessageType.TEXT; // Default type TEXT if not set

    @Column(name = "date_created", nullable = false, updatable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreated;

    @Column(name = "date_sent", nullable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateSent;

    @Column(name = "date_received")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateReceived;

    @Column(name = "date_read")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateRead;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now();
        this.dateSent = this.dateCreated;
        if (this.isRead == null) this.isRead = false;
        if (this.isDeleted == null) this.isDeleted = false;
    }
}
