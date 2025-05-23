package iset.pfe.mediconnectback.dtos;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MedecinResponse {
    
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String imageUrl;
    private String accountStatus;
    private String codeMedical;
    private Integer phoneNumber;
    private String workPlace;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    private Long startingPrice; // prix de consultation,
    private Boolean autoManageAppointments; // true = auto, false = manual
    private String Description;
    
}
