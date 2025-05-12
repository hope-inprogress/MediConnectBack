package iset.pfe.mediconnectback.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.dtos.AuthResponse;
import iset.pfe.mediconnectback.dtos.LoginRequest;
import iset.pfe.mediconnectback.dtos.MailBody;
import iset.pfe.mediconnectback.dtos.RefreshRequest;
import iset.pfe.mediconnectback.dtos.ResetPasswordBody;
import iset.pfe.mediconnectback.dtos.SignupRequest;
import iset.pfe.mediconnectback.entities.OTP;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.services.AdminService;
import iset.pfe.mediconnectback.services.AuthService;
import iset.pfe.mediconnectback.services.EmailService;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserService;
import iset.pfe.mediconnectback.services.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@RequestMapping("/auth")
@RestController
@CrossOrigin("http://localhost:5173/")
public class AuthController {
		
	@Autowired
	private UserService userService;
	
	@Autowired 
	private AuthService authService;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	
	@Autowired 
	private EmailService emailService;
	
	@Autowired
	private JwtService jwtService;

	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest registerRequest) {
	    try {
	        userService.registerUser(registerRequest);

	        Map<String, String> response = new HashMap<>();
	        response.put("message", "User registration successful!");
	        return ResponseEntity.status(HttpStatus.CREATED).body(response);
	    } catch (IllegalArgumentException e) {
	        // Handles duplicate email or password mismatch errors
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
	    } catch (RuntimeException e) {
	        // Handles unexpected registration failures
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body(Map.of("message", "User registration failed: " + e.getMessage()));
	    }
	}


	@PostMapping("/login")
	public ResponseEntity<AuthResponse> authenticate(
		@RequestBody LoginRequest request,
		HttpServletRequest httpRequest
	)  {
       String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            try {
                // Validate the token
                String existingEmail = jwtService.extractUserName(jwtToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(existingEmail);
                if (jwtService.isTokenValid(jwtToken, userDetails)) {
                    // Any valid token means a user is logged in
                    AuthResponse response = new AuthResponse();
                    response.setMessage("There is a user logged in on this browser.");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
            } catch (Exception e) {
                // Invalid token; proceed with login
                // Log for debugging: System.out.println("Token validation failed: " + e.getMessage());
            }
        }

	    return ResponseEntity.ok(userService.authenticateUser(request));
	}
	
	//send mail for email verification(forgot password)
	@PostMapping("/forgot-password")
	public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
		String email = request.get("email");
	    User user = userService.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("An account with this email does not exist!"));
	    
		String otp = authService.generateAndSendActivationToken(user); 
		MailBody mailBody = new MailBody();
		mailBody.setTo(email);
		mailBody.setSubject("Forgot password request");
		String url = "http://localhost:5173/reset-password?email=" + email + "&otp=" + otp;
		mailBody.setText("Hello " + user.getFullName() + ",\n\n"
		+ "Please confirm your email by clicking the link below:\n"
				+ url + "\n\n"
		+ "Your activation code is: " + otp);
			
		emailService.sendEmail(mailBody);
    	return new ResponseEntity<>("EmailSent for verification!", HttpStatus.OK);
	}
	
	@PostMapping("/forgot-password/resend")
	public ResponseEntity<String> resendEmail(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		User user = userService.findByEmail(email)
	            .orElseThrow(() -> new UsernameNotFoundException("An account with this email does not exist!"));
		
			authService.deletePreviousOTP(email, "RESET_PASSWORD");
			String otp = authService.generateAndSendActivationToken(user); 
			MailBody mailBody = new MailBody();
			mailBody.setTo(email);
			mailBody.setSubject("Forgot password request");
			String url = "http://localhost:5173/reset-password?email=" + email + "&otp=" + otp;
			mailBody.setText("Hello " + user.getFullName() + ",\n\n"
		            + "Please confirm your email by clicking the link below:\n"
		            + url + "\n\n"
		            + "Your activation code is: " + otp);
			
			emailService.sendEmail(mailBody);
			return new ResponseEntity<>("EmailSent for verification!", HttpStatus.OK);
		
	}
	
	//verify if OneTime password input is valid for that email
	@PostMapping("/verify-otp")
	public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> request) {
			    String email = request.get("email");
	    String otp = request.get("otp");
		Map<String, String> response = new HashMap<>();

	    User user = userService.findByEmail(email)
	    		.orElseThrow(() -> new UsernameNotFoundException("An account with this email does not exist!"));
	
	        OTP fp = authService.findByOtpAndUser(otp, user);
	        if (fp != null) {
	            if (fp.getExpiresAt().isBefore(LocalDateTime.now())) {
	                authService.deleteFp(fp.getId());
	                response.put("message", "OTP has expired");
	                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
	            }
	            response.put("message", "OTP verified successfully!");
	            return ResponseEntity.ok(response);
	        }
	        response.put("message", "OTP does not exist!");
	        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
	}
	
	@PutMapping("/resetPassword")
	public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordBody request ) {

        Map<String, String> response = authService.resetPassword(request);

		String message = response.get("message");
		
		if ("OTP has expired".equals(message)) {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
		} else if ("OTP does not exist!".equals(message)) {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
		} else if ("Password does not match!".equals(message)) {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
		} else if ("Password updated successfully!".equals(message)) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshRequest request) {
		AuthResponse response = new AuthResponse();
		try {
			response = userService.refreshToken(request.getRefreshToken());
			return ResponseEntity.ok(response);
		} catch (UsernameNotFoundException | IllegalArgumentException e) {
			response.setMessage("Invalid refresh token: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		} catch (Exception e) {
			response.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
