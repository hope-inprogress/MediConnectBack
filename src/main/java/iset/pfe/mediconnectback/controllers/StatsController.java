package iset.pfe.mediconnectback.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.services.MedecinService;
import iset.pfe.mediconnectback.services.PatientService;
import iset.pfe.mediconnectback.services.RendezVousService;
import iset.pfe.mediconnectback.services.UserService;

@RestController
@RequestMapping("/api/statistics")
public class StatsController {

    @Autowired
    private UserService userService;

    @Autowired
    private MedecinService medecinService;

    @Autowired 
    private PatientService patientService;

    @Autowired
    private RendezVousService rendezVousService;
    
       /**
     * Get statistics, Restricted to admin only.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/monthly/medecins")
      public ResponseEntity<List<Integer>> getMonthlyMedecinStats() {
         List<Integer> monthlyStats = medecinService.getMedecinsByMonth();
         return ResponseEntity.ok(monthlyStats);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/monthly/patients")
    public ResponseEntity<List<Integer>> getMonthlyPatientStats() {
       List<Integer> monthlyStats =patientService.getPatientsByMonth();
       return ResponseEntity.ok(monthlyStats);
   }

   @PreAuthorize("hasRole('MEDECIN')")
   @GetMapping("/rendezVous")
   public ResponseEntity<Map<String, Long>> getRendezVousStats() {
       Map<String, Long> stats = rendezVousService.getRendezVousStats();
       return ResponseEntity.ok(stats);
   }
   

   
}
