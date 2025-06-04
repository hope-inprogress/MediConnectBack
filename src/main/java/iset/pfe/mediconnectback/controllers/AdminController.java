package iset.pfe.mediconnectback.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import iset.pfe.mediconnectback.dtos.UserDTO;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService; // Inject JwtService


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> usersWithSpecialty = userService.getAllUsersWithSpecialite();
        return ResponseEntity.ok(usersWithSpecialty);
    }

   @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long userId) {
        String response = userService.activateUser(userId);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/reject")
    public ResponseEntity<String> rejeteeUser(@PathVariable Long userId, @RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
        
        String reason = request.get("reason");
        String description = request.get("description");
        Long adminId = jwtService.extractIdFromBearer(token);

        String response = userService.rejectUser(userId, adminId, reason, description);
        if (response.equals("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/block")
    public ResponseEntity<String> adminBlockUser(
        @PathVariable Long userId, 
        @RequestBody Map<String, String> request,
        @RequestHeader("Authorization") String token
    ) {
        try {
            String reason = request.get("reason");
            String description = request.get("description");
            Long adminId = jwtService.extractIdFromBearer(token);

            userService.blockUser(userId, adminId, reason, description);
            return ResponseEntity.ok("User blocked by admin successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error blocking the user: " + e.getMessage());
        }
    }

    // Unblock user method
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/unblock")
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

   /* @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId, @RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {

        String reason = request.get("reason");
        String description = request.get("description");
        Long adminId = jwtService.extractIdFromBearer(token);

        userService.deleteUser(userId, adminId, reason, description);
        return ResponseEntity.ok("User deleted successfully");
    }*/

}
