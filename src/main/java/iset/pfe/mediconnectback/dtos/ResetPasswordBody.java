package iset.pfe.mediconnectback.dtos;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordBody {

	private String email;

	private String otp;

	private String newPassword;
	
	private String repeatNewPassword;
	
}
