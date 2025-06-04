package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type",
  visible = true
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = UpdateMedecin.class, name = "Medecin"),
  @JsonSubTypes.Type(value = UpdatePatient.class, name = "Patient")
})
public class UpdateUser {

    private String email;

    private String firstName;

    private String lastName;

    private String address;

    private Integer phoneNumber;

    private LocalDate dateNaissance;

    private String Sexe;
    
}
