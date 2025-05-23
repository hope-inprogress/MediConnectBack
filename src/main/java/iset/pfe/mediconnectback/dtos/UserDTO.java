package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;

import iset.pfe.mediconnectback.enums.AccountStatus;
import iset.pfe.mediconnectback.enums.UserStatus;
import lombok.Data;

@Data // Lombok: Generates getters, setters, toString, etc.
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private Integer phoneNumber; // Changed to String for flexibility
    private LocalDate dateNaissance;
    private String sexe;
    private String imageUrl;
    private String role;
    private String codeMedical;
    private String specialite;
    private AccountStatus accountStatus;
    private UserStatus userStatus;
}
    
    



