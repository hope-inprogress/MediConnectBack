package iset.pfe.mediconnectback.services;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import iset.pfe.mediconnectback.dtos.SignupRequest;
import iset.pfe.mediconnectback.entities.Admin;
import iset.pfe.mediconnectback.entities.Motifs;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.AccountStatus;
import iset.pfe.mediconnectback.enums.EventType;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.repositories.AdminRepository;
import iset.pfe.mediconnectback.repositories.MotifsRepository;
import iset.pfe.mediconnectback.repositories.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class AdminService {


    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private MotifsRepository motifsRepo;

    @Autowired
    private UserRepository userRepo;

    public Admin createAdmin(SignupRequest request) {

        if (userRepo.findUserByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with this email");
        }        

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        //password and confirmPassword mismatches
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }

        Admin admin = new Admin();
	    admin.setFirstName(request.getFirstName());
	    admin.setLastName(request.getLastName());
	    admin.setEmail(request.getEmail());
	    admin.setPassword(authService.hashPassword(request.getPassword()));
	    admin.setUserStatus(UserStatus.Active);
	    admin.setAccountStatus(AccountStatus.Verified);
	    admin.setRole(UserRole.Admin);
	    admin.setCreatedDate(LocalDateTime.now());

        try {
	        // Save the user to the database
	        adminRepository.save(admin);
	    } catch (DataAccessException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
	    }

        return admin;

    }

    @Transactional
    public void deleteUserAccount(Long userId, Map<String, String> request) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

       /*  // Validation
        if (request.get("password") == null || request.get("repeatPassword") == null || request.get("reason") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password, repeat password and reason are required");
        }

        if (!request.get("password").equals(request.get("repeatPassword"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password and repeat password do not match");
        }

        if (!authService.verifyPassword(request.get("password"), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect password");
        }*/

        // make motif
        Motifs motif = new Motifs();
        motif.setEventType(EventType.USER_DELETED_BY_ADMIN);
        motif.setEventTime(LocalDate.now());
        motif.setReason(request.get("reason"));
        motif.setTargetUserId(user.getId());
        motif.setTargetUsername(user.getFirstName() + " " + user.getLastName());
        motif.setTargetUserImage(user.getImageUrl());
        motifsRepo.save(motif);

        userRepo.delete(user);
    }  

}






    
