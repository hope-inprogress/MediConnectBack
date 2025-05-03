package iset.pfe.mediconnectback.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {

	@Email(message = "Email is not formatted --> exemple@mail.com")
	@NotEmpty(message = " Email is mandatory")
	@NotBlank(message = " Email is mandatory")
    private String email;
    
	@NotEmpty(message = "Password is mandatory")
	@NotBlank(message = "Password is mandatory") 
	@Size(min = 8, message = "Password should be 8 characters long minimum")
	private String password;

	
}
