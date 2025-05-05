package iset.pfe.mediconnectback.dtos;

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
    private String accountStatus;
    private Integer phoneNumber;
}
