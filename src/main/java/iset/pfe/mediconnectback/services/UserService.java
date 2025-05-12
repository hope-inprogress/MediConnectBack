package iset.pfe.mediconnectback.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import iset.pfe.mediconnectback.dtos.AdminResponse;
import iset.pfe.mediconnectback.dtos.AuthResponse;
import iset.pfe.mediconnectback.dtos.ChangerPassword;
import iset.pfe.mediconnectback.dtos.LoginRequest;
import iset.pfe.mediconnectback.dtos.MedecinResponse;
import iset.pfe.mediconnectback.dtos.PatientResponse;
import iset.pfe.mediconnectback.dtos.SignupRequest;
import iset.pfe.mediconnectback.dtos.UpdateMedecin;
import iset.pfe.mediconnectback.dtos.UpdatePatient;
import iset.pfe.mediconnectback.dtos.UpdateUser;
import iset.pfe.mediconnectback.entities.Admin;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Token;
import iset.pfe.mediconnectback.entities.Motifs;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.AccountStatus;
import iset.pfe.mediconnectback.enums.EventType;
import iset.pfe.mediconnectback.enums.Sexe;
import iset.pfe.mediconnectback.enums.TokenType;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.repositories.MotifsRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import iset.pfe.mediconnectback.repositories.TokenRepository;
import iset.pfe.mediconnectback.repositories.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private AuthService authService;
		
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private TokenRepository tokenRepository;

    @Autowired
    private PatientRepository patientRepository;

	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private MotifsRepository motifsRepo;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepo;

	@Autowired
    @Value("${file.upload-dir}")
    private String uploadDir;


    public Optional<User> findByEmail(String email) {
		return userRepo.findUserByEmail(email);
		 
	}

	public AuthResponse authenticateUser(LoginRequest request) {
		authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
		
		User user = userRepo.findUserByEmail(request.getEmail())
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getEmail()));
		
       /*      // Check if the account is deleted
        if (user.getUserStatus()== UserStatus.Deleted) {
            throw new RuntimeException("Your account has been deleted");
        }*/

        // Check user status
        if (user.getUserStatus()== UserStatus.Undecided) {
            throw new RuntimeException("Your account is not yet activated. Please wait for admin approval");
        }

        if (user.getUserStatus() == UserStatus.Blocked) {
            throw new RuntimeException("Your account has been blocked. Please contact support");
        }

        if (user.getUserStatus() == UserStatus.Rejected) {
            throw new RuntimeException("Your account has been rejected. Please contact support");
        }

		String accessToken = jwtService.generateToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);

		saveAccessToken(user, accessToken);
        saveRefreshToken(user, refreshToken);

		AuthResponse authResponse = new AuthResponse();
		authResponse.setAccessToken(accessToken);
		authResponse.setRefreshToken(refreshToken);
		authResponse.setRole(user.getRole().name());
		authResponse.setMessage("User authenticated successfully");

		return authResponse;
	}

	private void saveAccessToken(User user, String jwtToken) {
		Token token = new Token();
		token.setUser(user);
		token.setTokenName("accessToken");
		token.setTokenType(TokenType.BEARER);
        token.setToken(jwtToken);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // Set expiration date for access token
        token.setCreatedAt(LocalDateTime.now());
		token.setExpired(false);
		token.setRevoked(false);
		tokenRepository.save(token);
	}

    private void saveRefreshToken(User user, String jwtToken) {
		Token token = new Token();
		token.setUser(user);
		token.setTokenName("refreshToken");
		token.setTokenType(TokenType.BEARER);
        token.setToken(jwtToken);
        token.setExpiresAt(LocalDateTime.now().plusDays(30)); // Set expiration date for refresh token
        token.setCreatedAt(LocalDateTime.now());
		token.setExpired(false);
		token.setRevoked(false);
		tokenRepository.save(token);
	}

	public void revokeToken(String token) {
		Optional<Token> validUserToken = tokenRepository.findByToken(token);

        if (validUserToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found");
        }
        Token tokenToRevoke = validUserToken.get();
        tokenToRevoke.setExpired(true);
        tokenToRevoke.setRevoked(true);
        tokenRepository.save(tokenToRevoke);
	}


	public AuthResponse refreshToken(String refreshToken) {
        Token storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        String email = jwtService.extractUserName(refreshToken);

        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        saveAccessToken(user, newAccessToken);
        saveRefreshToken(user, newRefreshToken); // Save new refresh token

        revokeToken(refreshToken); // Revoke old access tokens

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(newAccessToken);
        authResponse.setRefreshToken(newRefreshToken); // Reuse existing refresh token
        authResponse.setRole(user.getRole().name());
        authResponse.setMessage("Token refreshed successfully");

        return authResponse;
    }

	public User registerUser(SignupRequest request) {
	    // Check if email is already in use
	    Optional<User> userOp = userRepo.findUserByEmail(request.getEmail());
	    if (userOp.isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
	    }

	    // Check if passwords match
	    if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
	    }

	    // Validate role-specific fields
	    String role = request.getRole();
	    if ("Medecin".equalsIgnoreCase(role)) {
	        if (request.getCodeMedical() == null || request.getCodeMedical().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code Médical is required for Médecin");
	        }
	    } else if (!"Patient".equalsIgnoreCase(role)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role specified. Role must be 'Patient' or 'Medecin'");
	    }

	    // Create the appropriate user type based on the role
	    User user;
	    if ("Medecin".equalsIgnoreCase(role)) {
	        Medecin medecin = new Medecin();
	        medecin.setCodeMedical(request.getCodeMedical());
	        user = medecin; // Assign Medecin as a User
	    } else {
	        Patient patient = new Patient();
	        user = patient; // Assign Patient as a User
	    }

	    // Set common fields for all users
	    user.setFirstName(request.getFirstName());
	    user.setLastName(request.getLastName());
	    user.setEmail(request.getEmail());
	    user.setPassword(authService.hashPassword(request.getPassword()));
	    user.setUserStatus(UserStatus.Undecided);
	    user.setAccountStatus(AccountStatus.NotVerified);
	    user.setRole("Medecin".equalsIgnoreCase(role) ? UserRole.Medecin : UserRole.Patient);
	    user.setCreatedDate(LocalDateTime.now());
        user.setUpdatedDate(LocalDateTime.now());
        user.setImageUrl("/uploads/DefaultImage/Defaultimage.jpeg");

	    try {
	        // Save the user to the database
	        userRepo.save(user);

	    } catch (DataAccessException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
	    }

		try {
			authService.sendValidationEmail(user);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending validation email: " + e.getMessage());
		}
		return user;
	}

	public User findById(Long id) {
        return userRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));	
	}

    @Transactional
    public Object getMe(Long id) {
        User user = findById(id);

        if (user instanceof Medecin medecin) {
            // If the user is a Medecin, return MedecinResponse
            return new MedecinResponse(
                medecin.getFirstName(),
                medecin.getLastName(),
                medecin.getEmail(),
                medecin.getAddress(),
                medecin.getImageUrl(),
                medecin.getAccountStatus().name(),
                medecin.getCodeMedical(),
                medecin.getPhoneNumber()
            );

        } else if (user instanceof Admin admin) {
            // If the user is an Admin, return AdminResponse
            return new AdminResponse(
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail(),
                admin.getAddress(),
                admin.getImageUrl(),
                admin.getAccountStatus().name(),
                admin.getPhoneNumber()
            );
        } else if (user instanceof Patient patient) {
            // If the user is a Patient, return PatientResponse
            return new PatientResponse(
                
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getImageUrl(),
                patient.getAccountStatus().name(),
                patient.getPhoneNumber()
            );
        }
        throw new RuntimeException("Unsupported user type: " + user.getClass().getSimpleName());

    }

    @SuppressWarnings("null")
    @Transactional
    public User updateUserPhoto(Long id, MultipartFile photo) throws IOException {
        User user = findById(id);

        // Handle photo upload
        if (photo != null && !photo.isEmpty()) {
            // Validate photo
            if (!photo.getContentType().startsWith("image/") ) {
                throw new IllegalArgumentException("Le fichier doit être une image (JPEG, PNG, etc.)");
            }
            if (photo.getSize() > 5 * 1024 * 1024) { // 5MB limit
                throw new IllegalArgumentException("Le fichier est trop volumineux (max 5MB)");
            }

            // Create uploads directory if it doesn't exist
            String folderName = user.getFullName().replaceAll("\\s+", "_"); // replace spaces
            Path dynamicDir = Paths.get(uploadDir, folderName);
            File uploadDirFile = dynamicDir.toFile();
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // Delete old photo if exists
            if (user.getImageUrl() != null) {
                String oldImageFileName = user.getImageUrl().replace("/uploads/", "");
                Path oldPhotoPath = Paths.get(uploadDir, oldImageFileName);
                Files.deleteIfExists(oldPhotoPath);
            }

            // Save new photo
            String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
            Path filePath = dynamicDir.resolve(fileName);
            try {
                Files.write(filePath, photo.getBytes());
                System.out.println("File saved to: " + filePath.toString());
            } catch (IOException e) {
                throw new IOException("Failed to save the photo", e);
            }

            // Update photo path
            user.setImageUrl("/uploads/" + folderName + "/" + fileName);
        }

        return userRepo.save(user);
    }


    @Transactional
    public User changePassword(Long id, ChangerPassword request) {
		User user = findById(id);

        if(!authService.verifyPassword(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new RuntimeException("New password is required");
        }

        if (request.getNewPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        if (!request.getNewPassword().equals(request.getRepeatNewPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        user.setPassword(authService.hashPassword(request.getNewPassword()));
        user.setUpdatedDate(LocalDateTime.now());
        // Save the updated admin to the database
        try {
            userRepo.save(user);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
        // Return the updated admin object
        return user;
    }

    @Transactional
    public User updateUserData(Long id, UpdateUser request) throws IOException {
        User user = findById(id);

        // Update common user fields
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getDateNaissance() != null) user.setDateNaissance(request.getDateNaissance());

        // Handle Medecin-specific updates
        if (user instanceof Medecin medecin && request instanceof UpdateMedecin medecinRequest) {
            if (medecinRequest.getSpecialite() != null) medecin.setSpecialite(medecinRequest.getSpecialite());
            if (medecinRequest.getCodeMedical() != null) medecin.setCodeMedical(medecinRequest.getCodeMedical());
        }

        // Handle Patient-specific updates (if you have a subclass UpdatePatient)
        if (user instanceof Patient patient && request instanceof UpdatePatient patientRequest) {
            if (patientRequest.getCIN() != null) patient.setCIN(patientRequest.getCIN());
            // Add patient-specific fields here if needed
        }

        return userRepo.save(user);
    }

    @Transactional
    public void deleteMyAccount(Long myUserId, Map<String, String> request) {
        User user = userRepo.findById(myUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        //check if Password, repeatPassword and reason are not empty
        if (request.get("password") == null || request.get("repeatPassword") == null || request.get("reason") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password, repeat password and reason are required");
        }

        //check if Password and repeatPassword are the same
        if (!request.get("password").equals(request.get("repeatPassword"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password and repeat password do not match");
        }

        //check if the Password is Correct
        if (!authService.verifyPassword(request.get("password"), user.getPassword() )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect password");
        }

        String reason = request.get("reason");
        String description = request.get("description") != null ? request.get("description") : "No description provided";

        // Log the deletion reason
        Motifs motif = new Motifs();
        motif.setEventType(EventType.USER_DELETED_HIS_ACCOUNT);
        motif.setEventTime(LocalDate.now());
        motif.setReason(reason);
        motif.setTargetUser(user);
        motif.setPerformedBy(user); // Assuming the user is performing the action
        motif.setDescription(description);
        motifsRepo.save(motif);
        
        // Delete the user
        userRepo.delete(user);
    }

    public boolean isPatient(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole().equals(UserRole.Patient); // Adjust based on your enum or string
    }
    
























	public List<User> getAllUsers() {
		return userRepo.findAll();
	}

    //getAll Users except Admins
	public List<User> getAllUsersWithSpecialite() {
        return userRepo.findAllByRoleNot(UserRole.Admin);
    }

	@Transactional
    public String activateUser(Long userId) {
        Optional<User> userOptional = userRepo.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setUserStatus(UserStatus.Active);
            userRepo.save(user);
            return "User activated successfully";
        } else {
            return "User not found";
        }
    }

    // Method to block a user
    @Transactional
    public void blockUser(Long targetUserId, Long performedByUserId, String reason, String description) {
        User targetUser = findById(targetUserId);
        User performedBy = findById(performedByUserId);

        // Case 1: Medecin blocking a Patient (soft block)
        if (targetUser instanceof Patient patient && performedBy instanceof Medecin medecin) {
            Set<String> blockedBy = new HashSet<>(
                Optional.ofNullable(patient.getBlockedByMedecinIds())
                        .map((String ids) -> java.util.Arrays.asList(ids.split(",")))
                        .orElse(Collections.emptyList())
            );

            if (blockedBy.contains(medecin.getId().toString())) {
                throw new RuntimeException("Patient already blocked by this medecin");
            }

            blockedBy.add(medecin.getId().toString());
            patient.setBlockedByMedecinIds(String.join(",", blockedBy));
            patientRepository.save(patient);

            Motifs motif = new Motifs();
            motif.setEventType(EventType.PATIENT_BLOCKED_BY_MEDECIN);
            motif.setEventTime(LocalDate.now());
            motif.setReason(reason);
            motif.setDescription(description);
            motif.setTargetUser(patient);
            motif.setPerformedBy(medecin);
            motifsRepo.save(motif);

        } else {
            // Case 2: Admin blocking a User (hard block)
            if (targetUser.getUserStatus() == UserStatus.Blocked) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already blocked");
            }

            targetUser.setUserStatus(UserStatus.Blocked);
            userRepo.save(targetUser);

            Motifs motif = new Motifs();
            motif.setEventType(EventType.USER_BLOCKED_BY_ADMIN);
            motif.setEventTime(LocalDate.now());
            motif.setReason(reason);
            motif.setDescription(description);
            motif.setTargetUser(targetUser);
            motif.setPerformedBy(performedBy);
            motifsRepo.save(motif);
        }
    }


	public String rejectUser(Long targetUserId, Long adminId, String reason, String description) {
        User user = userRepo.findById(targetUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        User admin = userRepo.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        user.setUserStatus(UserStatus.Rejected);
        userRepo.save(user);
    
        Motifs motif = new Motifs();
        motif.setEventType(EventType.USER_REJECTED_BY_ADMIN);
        motif.setEventTime(LocalDate.now());
        motif.setReason(reason);
        motif.setDescription(description);
        motif.setTargetUser(user);
        motif.setPerformedBy(admin);
        motifsRepo.save(motif);
    
        return "User rejected successfully";
    }
	
	@Transactional
	public void unblockUser(Long userId) {
		User user = userRepo.findById(userId)
			.orElseThrow(() -> new RuntimeException("User not found"));
	
		// Ensure the user is actually blocked
		if (user.getUserStatus() != UserStatus.Blocked) {
			throw new RuntimeException("User is not blocked");
		}
	
		// Update statut_compte to "active"
		user.setUserStatus(UserStatus.Active);
		userRepo.save(user);
	}

    /*@Transactional
    public void deleteUser(Long targetUserId, Long adminId, String reason, String description) {
        User user = userRepo.findById(targetUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    
        User admin = userRepo.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
    
        // Create the motif BEFORE modifying user
        Motifs motif = new Motifs();
        motif.setEventType(EventType.USER_DELETED_BY_ADMIN);
        motif.setEventTime(LocalDate.now());
        motif.setReason(reason);
        motif.setDescription(description);
        motif.setTargetUser(user);     // ✅ Managed entity
        motif.setPerformedBy(admin);   // ✅ Managed entity
    
        motifsRepo.save(motif); // Save while both user/admin are still in DB
    
        // Clean up user associations
        user.getTokens().clear();
        if (user instanceof Patient patient) {
            patient.getAppointments().clear();
            patient.getDocumentsMedicaux().clear();
            if (patient.getDossierMedical() != null) {
                dossierMedicalRepo.delete(patient.getDossierMedical());
            }
        } else if (user instanceof Medecin medecin) {
            medecin.getAppointments().clear();
            medecin.getDocumentsMedicaux().clear();
            if (medecin.getDossiersMedicaux() != null) {
                medecin.getDossiersMedicaux().forEach(dossier -> dossier.setMedecin(null));
            }
        }
    
        userRepo.delete(user);
    }*/

	public Long countMedecins() {
        return userRepo.countByRole(UserRole.Medecin);
    }
    
    public Long countPatients() {
        return userRepo.countByRole(UserRole.Patient);
    }
    
    public Long countBlockedAccounts() {
        
        return userRepo.countByUserStatus(UserStatus.Blocked);
    }
    
    public Long countActiveAccounts() {
       
        return userRepo.countByUserStatus(UserStatus.Active);
    }
    
    public Long countPendingAccounts() {
       
        return userRepo.countByUserStatus(UserStatus.Undecided);
    }
    public Long countRejectedAccounts() {
        return userRepo.countByUserStatus(UserStatus.Rejected);
    }
    
    public Long countMaleDoctors() {
        return userRepo.countByRoleAndSexe(UserRole.Medecin, Sexe.Homme);
    }
    
    public Long countFemaleDoctors() {
        return userRepo.countByRoleAndSexe(UserRole.Medecin, Sexe.Femme);
    }
    
    public Long countMalePatients() {
        return userRepo.countByRoleAndSexe(UserRole.Patient, Sexe.Homme);
    }
    
    public Long countFemalePatients() {
        return userRepo.countByRoleAndSexe(UserRole.Patient, Sexe.Femme);
    }
    

    public Map<String, Long> getGenderStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        // Using the derived methods to get the counts
        stats.put("maleDoctors", userRepo.countByRoleAndSexe(UserRole.Medecin, Sexe.Homme));
        stats.put("femaleDoctors", userRepo.countByRoleAndSexe(UserRole.Medecin, Sexe.Femme));
        stats.put("malePatients", userRepo.countByRoleAndSexe(UserRole.Patient, Sexe.Homme));
        stats.put("femalePatients", userRepo.countByRoleAndSexe(UserRole.Patient, Sexe.Femme));
        
        return stats;
    }


    public Long countTotalUsers() {
        return userRepo.count();
    }

	
}
