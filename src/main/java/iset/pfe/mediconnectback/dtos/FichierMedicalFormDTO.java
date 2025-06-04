package iset.pfe.mediconnectback.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FichierMedicalFormDTO {
        // Basic Health Info
    private String bloodType;
    private Double height; // in cm
    private Double weight; // in kg

    private String allergies;

    private String chronicDiseases;

    private String currentMedications;

    private String surgicalHistory;

    private String familyMedicalHistory;

    // Optional: Lifestyle or other useful notes
    private Boolean smoker;
    private Boolean alcoholUse;
    private String activityLevel; // e.g. Sedentary, Moderate, Active
    private String dietaryPreferences;

    //constractor 
    

}
