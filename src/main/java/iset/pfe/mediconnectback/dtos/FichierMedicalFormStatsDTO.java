package iset.pfe.mediconnectback.dtos;

import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FichierMedicalFormStatsDTO {
    private Double weight;
    private Double height;
    private Boolean smoker;
    private Boolean alcoholUse;
    private String activityLevel;
    private String dietaryPreferences;
    private LocalDateTime createdAt;
}
