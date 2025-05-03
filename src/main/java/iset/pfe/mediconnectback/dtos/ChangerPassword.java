package iset.pfe.mediconnectback.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangerPassword {
   
    private String oldPassword;

    private String newPassword;
	
	private String repeatNewPassword;
}
