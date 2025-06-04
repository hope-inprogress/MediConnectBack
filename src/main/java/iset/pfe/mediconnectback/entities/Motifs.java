package iset.pfe.mediconnectback.entities;

import java.time.LocalDate;

import iset.pfe.mediconnectback.dtos.UserDTO;
import iset.pfe.mediconnectback.enums.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Motifs {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @NotNull
    private LocalDate eventTime;

    @NotNull
    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String Description;

    // The user who is the subject of this motif (e.g., the one being blocked), get only the id of the user

    //@ManyToOne
    //@JoinColumn(name = "userId")
    private Long targetUserId;

    private String targetUsername;

    private String targetUserImage;
    
}
