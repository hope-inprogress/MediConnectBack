package iset.pfe.mediconnectback.entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class MedecinHoliday {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;
}
