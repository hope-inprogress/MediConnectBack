package iset.pfe.mediconnectback.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import iset.pfe.mediconnectback.dtos.ChangerPassword;
import iset.pfe.mediconnectback.dtos.UpdateUser;
import iset.pfe.mediconnectback.dtos.UserResponse;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.services.AuthorizationService;
import iset.pfe.mediconnectback.services.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private AuthorizationService authorizationService; // Inject AuthorizationService

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersWithSpecialty = userService.getAllUsersWithSpecialite();
        return ResponseEntity.ok(usersWithSpecialty);
    }


    @PreAuthorize("@authorizationService.isSelf(#id, authentication)")
    @GetMapping("/me/{id}")
    public ResponseEntity<UserResponse> getMe(@PathVariable Long id, Authentication authentication) {
        // We assume the check has already been done in the AuthorizationService
        User user = userService.findById(id);

        String codeMedical = null;
        if (user instanceof Medecin) {  // Check if user is a Medecin
            Medecin medecin = (Medecin) user;  // Cast the user to Medecin
            codeMedical = medecin.getCodeMedical();  // Retrieve the CodeMedical for Medecin
        }

        // Convert User to UserResponse, including CodeMedical for Medecin
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAddress(),
                user.getImageUrl(),
                user.getAccountStatus().name(),
                codeMedical,// Pass the CodeMedical value, null if not a Medecin
                user.getPhoneNumber()// Pass the phone number
        );

        return ResponseEntity.ok(userResponse);
    }

    @PreAuthorize("@authorizationService.isSelf(#id, authentication)")
    @PutMapping("/{id}/change-password")
    public ResponseEntity<User> changePassword(@PathVariable Long id, @RequestBody ChangerPassword request) {
        
        User user = userService.changePassword(id, request);

        return ResponseEntity.ok(user);
    }

    @PreAuthorize("@authorizationService.isSelf(#id, authentication)")
    @PutMapping("/{id}/data")
    public ResponseEntity<?> updateUserData(@PathVariable Long id, @RequestBody UpdateUser request) {
        try {
            User updated = userService.updateUserData(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur inattendue : " + e.getMessage());
        }
    }

    @PreAuthorize("@authorizationService.isSelf(#id, authentication)")
    @PutMapping(value = "/{id}/photo", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateUserPhoto(
        @PathVariable Long id,
        @RequestPart(value = "imageUrl", required = false) MultipartFile photo) {
        try {
            User user = userService.updateUserPhoto(id, photo);
            return ResponseEntity.ok(user);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour du profil : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur inattendue : " + e.getMessage());
        }
    }
   
    @PreAuthorize("@authorizationService.isSelf(#id, authentication)")
    @DeleteMapping("/deleteMe/{id}")
    public ResponseEntity<Void> deleteMe(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        userService.deleteMyAccount(id, request);
        return ResponseEntity.noContent().build();
    }





   @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{userId}")
    public ResponseEntity<String> activateUser(@PathVariable Long userId) {
        String response = userService.activateUser(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block/{userId}")
    public ResponseEntity<String> blockUser(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            // Extract reason for blocking from request body
            String reason = request.get("reason");

            // Call the service method to block the user
            userService.blockUser(userId, reason);

            // Return a success response
            return ResponseEntity.ok("User blocked successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error blocking the user: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/rejetee/{userId}")
    public ResponseEntity<String> rejeteeUser(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        String response = userService.rejectUser(userId, reason);
        if (response.equals("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    // Unblock user method
    @PutMapping("/unblock/{userId}")
    public ResponseEntity<String> unblockUser(@PathVariable Long userId) {
        try {
            // Call the service method to unblock the user
            userService.unblockUser(userId);

            // Return a success response
            return ResponseEntity.ok("User unblocked successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error unblocking the user: " + e.getMessage());
        }
    }

  
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, @RequestBody String reason) {
        userService.deleteUser(userId, reason);
        return ResponseEntity.noContent().build();
    }

   @PreAuthorize("hasRole('ADMIN')")
   @GetMapping("/total")
   public Map<String, Long> getUserStats() {
    Map<String, Long> stats = new HashMap<>();
    
    // Using the derived methods to get counts based on role and account status
    stats.put("medecins", userService.countMedecins());
    stats.put("patients", userService.countPatients());
    stats.put("blocked", userService.countBlockedAccounts());
    stats.put("active", userService.countActiveAccounts());
    stats.put("pending", userService.countPendingAccounts());
    stats.put("rejected", userService.countRejectedAccounts());
    stats.put("total", userService.countTotalUsers());

    
    return stats;
}

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/gender")
    public Map<String, Long> getGenderStatistics() {
        return userService.getGenderStatistics();
    }
   

}
