package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUser {

    private String firstName;

    private String lastName;

    private String email;

    private String address;

    private Integer phoneNumber;

    private LocalDate dateNaissance;

    private String Sexe;

    private String imageUrl;
    
}
