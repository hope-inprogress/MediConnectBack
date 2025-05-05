package iset.pfe.mediconnectback.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import iset.pfe.mediconnectback.dtos.ChangerPassword;
import iset.pfe.mediconnectback.dtos.UpdateUser;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService; // Inject JwtService

    // 🔍 GET current user profile
    // 🔍 GET current user profile
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String tokenHeader) {
        // Extract the ID from the Token
        String token = tokenHeader.replace("Bearer ", "");
        Long userId = jwtService.extractId(token);

        Object userResponse = userService.getMe(userId);
        return ResponseEntity.ok(userResponse);

    }


    // 🔒 CHANGE password
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/password")
    public ResponseEntity<User> changePassword(@RequestBody ChangerPassword request, @RequestHeader("Authorization") String tokenHeader) {
        
        String token = tokenHeader.replace("Bearer ", "");

        // Get extract the ID from the Token
        Long id = jwtService.extractId(token);
        User user = userService.changePassword(id, request);

        return ResponseEntity.ok(user);
    }

    // 📝 UPDATE user data
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/data")
    public ResponseEntity<?> updateUserData(@RequestHeader("Authorization") String tokenHeader, @RequestBody UpdateUser request) {
        try {
            String token = tokenHeader.replace("Bearer ", "");

            Long id = jwtService.extractId(token);
            // Call the service to update user data
            userService.updateUserData(id, request);
            var updated = getMe(tokenHeader).getBody();
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur inattendue : " + e.getMessage());
        }
    }

    // 🖼️ UPDATE profile photo
    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/me/photo", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateUserPhoto(

        @RequestHeader("Authorization") String tokenHeader,
        @RequestPart(value = "imageUrl", required = false) MultipartFile photo) {
        try {
            String token = tokenHeader.replace("Bearer ", "");

            Long id = jwtService.extractId(token);
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
   
    // 🗑️ DELETE account
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("me/deleteMe")
    public ResponseEntity<Void> deleteMe(@RequestHeader("Authorization") String tokenHeader, @RequestBody Map<String, String> request) {
        String token = tokenHeader.replace("Bearer ", "");

        Long id = jwtService.extractId(token);
        userService.deleteMyAccount(id, request);
        return ResponseEntity.noContent().build();
    }
























    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersWithSpecialty = userService.getAllUsersWithSpecialite();
        return ResponseEntity.ok(usersWithSpecialty);
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
