package iset.pfe.mediconnectback.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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
	
	private final String  confirmationUrl = "http://localhost:5173/auth/account-verifivation";
	
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
	
	public void resetPassword(String email, String RawPassword) {
		Optional<User> user = userRepo.findUserByEmail(email);
		String newPassword = hashPassword(RawPassword);
		user.get().setPassword(newPassword);
		userRepo.save(user.get());
	}

	public void sendValidationEmail(User user) {
		String newToken = generateAndSendActivationToken(user);
		
		// Prepare the email details
	    MailBody mailBody = new MailBody();
	    mailBody.setTo(user.getEmail());  // Set the recipient's email address
	    mailBody.setSubject("Please Confirm Your Email Address");
	    mailBody.setText("Hello " + user.getFullName() + ",\n\n"
	            + "Please confirm your email by clicking the link below:\n"
	            + confirmationUrl + "\n\n"
	            + "Your activation code is: " + newToken);
	    
		emailService.sendEmail(mailBody);	
		
	}

	public String generateAndSendActivationToken(User user) {
		String generatedOTP = generateActivationCode(6);
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
	
	
	public void activateAccount(String token) throws MessagingException{
		OTP savedToken = otpRepo.findByOtp(token)
				.orElseThrow(() -> new RuntimeException("Invalid token"));
		if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
			sendValidationEmail(savedToken.getUser());
			throw new RuntimeException("Activation Expired. A new token has been sent to the same email address!");
		}
		
		var user = userRepo.findById(savedToken.getUser().getId())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		user.setAccountStatus(AccountStatus.Verified);
		user.setUpdatedDate(LocalDateTime.now());
		userRepo.save(user);
		savedToken.setValidatedAt(LocalDateTime.now());
		otpRepo.save(savedToken);
	}
	
	@Transactional
	public void deletePreviousOTP(String email, String name) {
		otpRepo.deleteByUserEmailAndName(email, name);
	}

	
}