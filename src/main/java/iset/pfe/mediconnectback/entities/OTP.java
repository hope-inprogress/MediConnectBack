package iset.pfe.mediconnectback.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OTP {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String otp;
	
	private LocalDateTime createdAt;
	private LocalDateTime expiresAt;
	private LocalDateTime validatedAt;
	private String name;
	
	
	
	@ManyToOne
	@JoinColumn(name = "userId", nullable = false)
	private User user;
	
}

