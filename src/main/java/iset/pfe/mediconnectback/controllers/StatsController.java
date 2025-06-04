package iset.pfe.mediconnectback.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.dtos.MedecinDTO;
import iset.pfe.mediconnectback.dtos.PatientDTO;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.services.JwtService;
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

    @Autowired
    private JwtService jwtService;
    
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rendezVous")
    public ResponseEntity<Map<String, Long>> getRendezVousStats() {
        Map<String, Long> stats = rendezVousService.getRendezVousStats();
        return ResponseEntity.ok(stats);
    }
   
    // count Active Patients for a speceficMedeci
    
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/patients/active")
    public ResponseEntity<Integer> getActivePatientsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<PatientDTO> patients = patientService.getPatientsByMedecin(medecinId);
        List<PatientDTO> activePatients = new ArrayList<>();
        for (PatientDTO patient : patients) {
            if (patient.getUserStatus() == UserStatus.Active) {
                activePatients.add(patient);
            }
        }
        Integer size = activePatients.size();
        return ResponseEntity.ok(size);

    }

    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/patients/blocked")
    public ResponseEntity<Integer> getBlockedPatientsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<PatientDTO> patients = patientService.getPatientsByMedecin(medecinId);
        List<PatientDTO> blockedPatients = new ArrayList<>();
        for (PatientDTO patient : patients) {
            if (patient.getUserStatus() == UserStatus.Blocked) {
                blockedPatients.add(patient);
            }
        }
        Integer size = blockedPatients.size();
        return ResponseEntity.ok(size);

    }

    // get total patients for a specific medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/patients/total")
    public ResponseEntity<Integer> getTotalPatientsByMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<PatientDTO> patients = patientService.getPatientsByMedecin(medecinId);
        Integer size = patients.size();
        return ResponseEntity.ok(size);

    }

    // get total appointments for a specific medecin
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/appointements/total")
    public ResponseEntity<Integer> getTotalAppointmentsForMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<RendeVousDTO> appointments = medecinService.getAppointmentsByMedecin(medecinId);
        Integer size = appointments.size();
        return ResponseEntity.ok(size);
    }

    // get total appointments for a specific patient
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/count/mes-medecins")
    public ResponseEntity<Long> countMesMedecin(@RequestHeader("Authorization") String token) {
        Long userId = jwtService.extractIdFromBearer(token);
        List<MedecinDTO> medecins = medecinService.getMedecinsByPatient(userId);
        Long count = (long) medecins.size();
        return ResponseEntity.ok(count);
    }

    // get total rendezVous for a specefic patient
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/count/rendezvous")
    public ResponseEntity<Long> countMesRendezVous(@RequestHeader("Authorization") String token) {
        Long userId = jwtService.extractIdFromBearer(token);
        List<RendeVousDTO> rendezVous = patientService.getAppointmentsByPatient(userId);
        Long count = (long) rendezVous.size();
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/count/favorites")
    public ResponseEntity<Long> countFavorites(@RequestHeader("Authorization") String token) {
        Long patientId = jwtService.extractIdFromBearer(token);
        List<MedecinDTO> medecins = patientService.getFavoriteMedecins(patientId);
        Long count = (long) medecins.size();
        return ResponseEntity.ok(count);
    }
   
}
