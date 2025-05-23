package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PatientResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String imageUrl;
    private String accountStatus;  // Changed from AccountStatus to String
    private Integer phoneNumber;
    private LocalDate dateNaissance;
    private String sexe;  // Changed from Sexe to String
}
