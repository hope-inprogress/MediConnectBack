package iset.pfe.mediconnectback.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserService;
import iset.pfe.mediconnectback.services.MedecinService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService; // Inject JwtService

    @Autowired
    private MedecinService medecinService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersWithSpecialty = userService.getAllUsersWithSpecialite();
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
    public ResponseEntity<String> rejeteeUser(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        
        String reason = request.get("reason");
        String description = request.get("description");
        Long adminId = jwtService.extractId(request.get("token").replace("Bearer ", ""));

        String response = userService.rejectUser(userId, adminId, reason, description);
        if (response.equals("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/block")
    public ResponseEntity<String> adminBlockUser(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            String description = request.get("description");
            Long adminId = jwtService.extractId(request.get("token").replace("Bearer ", ""));

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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, @RequestBody Map<String, String> request) {

        String reason = request.get("reason");
        String description = request.get("description");
        Long adminId = jwtService.extractId(request.get("token").replace("Bearer ", ""));

        userService.deleteUser(userId, adminId, reason, description);
        return ResponseEntity.noContent().build();
    }

   @PreAuthorize("hasRole('ADMIN')")
   @GetMapping("/users/statistics")
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
    @GetMapping("/users/statistics/gender")
    public Map<String, Long> getGenderStatistics() {
        return userService.getGenderStatistics();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/medecins/monthly-stats")
      public ResponseEntity<List<Integer>> getMonthlyPatientStats() {
         List<Integer> monthlyStats = medecinService.getMedecinsByMonth();
         return ResponseEntity.ok(monthlyStats);
    }
    

}
