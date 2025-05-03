package iset.pfe.mediconnectback.services;

import java.time.LocalDateTime;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import iset.pfe.mediconnectback.dtos.SignupRequest;
import iset.pfe.mediconnectback.entities.Admin;
import iset.pfe.mediconnectback.enums.AccountStatus;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.repositories.AdminRepository;
import iset.pfe.mediconnectback.repositories.UserRepository;

@Service
public class AdminService {


    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AuthService authService;

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

}






    
