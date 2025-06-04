package iset.pfe.mediconnectback.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {

  private String accessToken;
  
  private String refreshToken;

  private Long id;

  private String role;

  private String message;
}
