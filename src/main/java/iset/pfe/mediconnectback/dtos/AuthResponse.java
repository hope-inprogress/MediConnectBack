package iset.pfe.mediconnectback.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {

  @JsonProperty("access_token")
  private String accessToken;
  
  @JsonProperty("refresh_token")
  private String refreshToken;

  private String role;

  private String message;
}
