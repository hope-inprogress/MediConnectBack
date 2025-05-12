package iset.pfe.mediconnectback.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService; // Inject JwtService

    // 🔍 GET current user profile
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String tokenHeader) {
        // Extract the ID from the Token
        Long userId = jwtService.extractIdFromBearer(tokenHeader);

        Object userResponse = userService.getMe(userId);
        return ResponseEntity.ok(userResponse);

    }

    // 🔒 CHANGE password
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/change-password")
    public ResponseEntity<User> changePassword(@RequestBody ChangerPassword request, @RequestHeader("Authorization") String tokenHeader) {
        
        // Get extract the ID from the Token
        Long id = jwtService.extractIdFromBearer(tokenHeader);
        User user = userService.changePassword(id, request);

        return ResponseEntity.ok(user);
    }

    // 📝 UPDATE user data
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/update-data")
    public ResponseEntity<?> updateUserData(@RequestHeader("Authorization") String tokenHeader, @RequestBody UpdateUser request) {
        try {

            Long id = jwtService.extractIdFromBearer(tokenHeader);
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
    @PutMapping(value = "/me/update-photo", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateUserPhoto(

        @RequestHeader("Authorization") String tokenHeader,
        @RequestPart(value = "imageUrl", required = false) MultipartFile photo) {
        try {
            Long id = jwtService.extractIdFromBearer(tokenHeader);

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
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@RequestHeader("Authorization") String tokenHeader, @RequestBody Map<String, String> request) {
        
        Long id = jwtService.extractIdFromBearer(tokenHeader);

        userService.deleteMyAccount(id, request);
        return ResponseEntity.noContent().build();
    }

}
