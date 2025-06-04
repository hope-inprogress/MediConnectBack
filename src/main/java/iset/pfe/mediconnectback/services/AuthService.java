package iset.pfe.mediconnectback.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
//import java.util.Optional;
//import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.dtos.MailBody;
import iset.pfe.mediconnectback.dtos.ResetPasswordBody;
import iset.pfe.mediconnectback.entities.OTP;

import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.AccountStatus;
import iset.pfe.mediconnectback.repositories.OTPRepository;
import iset.pfe.mediconnectback.repositories.TokenRepository;
import iset.pfe.mediconnectback.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;


@Service
public class AuthService {
	
	
	@Autowired 
	private PasswordEncoder encoder;
	
	@Autowired 
	private UserRepository userRepo;
	
	@Autowired
	private TokenRepository tokenRepo;

	@Autowired
	private OTPRepository otpRepo;
	
	@Autowired
	private EmailService emailService;
	
	private final String  confirmationUrl = "http://localhost:5173/account-verifivation";
	
	public void deleteFp(Long id) {
		tokenRepo.deleteById(id);
	}
	
	public OTP findByOtpAndUser(String token, User user) {
		Optional<OTP> fp =otpRepo.findByOtpAndUser(token, user);
		if (fp.isPresent()) {
			return fp.get();
		}
		return null;
	}
	
	public String hashPassword(String rawPassword) {
		return encoder.encode(rawPassword);
	}
	
	public boolean verifyPassword(String rawPassword, String encodedPassword) {
        // Exemple simple (à remplacer par une vérification sécurisée comme BCrypt)
        return encoder.matches(rawPassword, encodedPassword);
	}
	
	public User updatePassword(User user, String RawPassword) {
		String newPassword = hashPassword(RawPassword);
		user.setPassword(newPassword);
		return userRepo.save(user);
	}
	
	public Map<String, String> resetPassword(ResetPasswordBody request) {

		Map<String, String> response = new HashMap<>();
		
		User user = userRepo.findUserByEmail(request.getEmail())
		    .orElseThrow(() -> new UsernameNotFoundException("An account with this email does not exist!"));
		
		OTP fp = findByOtpAndUser(request.getOtp(), user);

		if (fp == null) {
			response.put("message", "OTP does not exist!");
			return response;
		}

		if (fp.getExpiresAt().isBefore(LocalDateTime.now())) {
			deleteFp(fp.getId());
			response.put("message", "OTP has expired");
			return response;
		}
		String rawPassword = request.getNewPassword();
		if (!Objects.equals(rawPassword, request.getRepeatNewPassword())) {
			response.put("message", "Password does not match!");
			return response;
		}		

		String newPassword = hashPassword(rawPassword);
		user.setPassword(newPassword);
		userRepo.save(user);
		response.put("message", "Password updated successfully!");
		return response;
	}

	@Transactional
	public void sendValidationEmail(User user) {
		// Ensure the user is in a managed state
		User managedUser = userRepo.findById(user.getId())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
				
		String newToken = generateAndSendValidationToken(managedUser);
		
		// Prepare the email details
	    MailBody mailBody = new MailBody();
	    mailBody.setTo(managedUser.getEmail());  // Set the recipient's email address
	    mailBody.setSubject("Please Confirm Your Email Address");
		String url = confirmationUrl + "?token=" + newToken;
	    mailBody.setText("Hello " + managedUser.getFullName() + ",\n\n"
	            + "Please confirm your email by clicking the link below:\n"
	            + url + "\n\n"
	            + "Your activation code is: " + newToken);
	    
		emailService.sendEmail(mailBody);	
	}

	@Transactional
	public String generateAndSendValidationToken(User user) {
		// Ensure the user is in a managed state
		User managedUser = userRepo.findById(user.getId())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
				
		String generatedOTP;

		do {
			generatedOTP = generateActivationCode(6);
		} while (otpRepo.findByOtp(generatedOTP).isPresent());
		
		OTP otp = new OTP();
		otp.setOtp(generatedOTP);
		otp.setName("VALIDATION");  // Changed from RESET_PASSWORD to VALIDATION
		otp.setCreatedAt(LocalDateTime.now());
		otp.setExpiresAt(LocalDateTime.now().plusMinutes(15));
		otp.setUser(managedUser);  // Use the managed user entity
		
		otpRepo.save(otp);
		return generatedOTP;
	}

	public String generateAndSendResetToken(User user) {
		String generatedOTP;

		do {
			generatedOTP = generateActivationCode(6);
		} while (otpRepo.findByOtp(generatedOTP).isPresent());
		
		OTP otp = new OTP();
		otp.setOtp(generatedOTP);
		otp.setName("RESET_PASSWORD");
		otp.setCreatedAt(LocalDateTime.now());
		otp.setExpiresAt(LocalDateTime.now().plusMinutes(15));
		otp.setUser(user);
		
		otpRepo.save(otp);
		return generatedOTP;
	}

	private String generateActivationCode(int length) {
		String characters = "0123456789";
		StringBuilder codeBuilder = new StringBuilder();
		SecureRandom secureRandom = new SecureRandom();
		for ( int i = 0; i < length; i++) {
			int randomIndex = secureRandom.nextInt(characters.length());
			codeBuilder.append(characters.charAt(randomIndex));
		}
		return codeBuilder.toString();
	}

	public boolean isUserLoggedIn() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.isAuthenticated();
	}
	
	
	public boolean isAdmin() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    return authentication != null &&
	           authentication.isAuthenticated() &&
	           authentication.getAuthorities().stream()
	               .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
	}
	
	
	@Transactional
	public void activateAccount(String otp, String email) throws MessagingException {
		// First find the OTP
		OTP savedToken = otpRepo.findByOtp(otp)
				.orElseThrow(() -> new RuntimeException("Invalid otp for email validation, check your email again!"));
		
		// Check if token is already validated
		if(savedToken.getValidatedAt() != null) {
			throw new RuntimeException("Token already validated");
		}
		
		// Check if token is expired
		if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
			sendValidationEmail(savedToken.getUser());
			throw new RuntimeException("Activation Expired. A new token has been sent to the same email address!");
		}
		
		// Get the user from the token instead of querying by email
		User user = savedToken.getUser();
		if (user == null) {
			throw new RuntimeException("User not found for this token");
		}
		
		// Update user status
		user.setAccountStatus(AccountStatus.Verified);
		user.setUpdatedDate(LocalDateTime.now());
		userRepo.save(user);
		
		// Update token
		savedToken.setValidatedAt(LocalDateTime.now());
		otpRepo.save(savedToken);
	}
	
	@Transactional
	public void deletePreviousOTP(String email, String name) {
		otpRepo.deleteByUserEmailAndName(email, name);
	}

}