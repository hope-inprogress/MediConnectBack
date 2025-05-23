package iset.pfe.mediconnectback.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignupRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

   
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "Invalid email format (must have valid domain like .com, .net, etc.)"
    )
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email is too long")
    private String email;
    


    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @Pattern(
    regexp = "^TM-\\d{4}$",
    message = "Code medical must start with TM and be followed by a hyphen and 4 digits (e.g., TM-2348)"
)
    private String codeMedical;



    @NotBlank(message = "Role is required")
    private String role; // "Patient" or "Medecin"
    

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }
    
}
