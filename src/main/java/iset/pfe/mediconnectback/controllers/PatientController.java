package iset.pfe.mediconnectback.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.services.PatientService;

@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "http://localhost:5173")
public class PatientController {
  
    @Autowired
    private PatientService patientService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/monthly")
    public ResponseEntity<List<Integer>> getMonthlyPatientStats() {
       List<Integer> monthlyStats =patientService.getPatientsByMonth();
       return ResponseEntity.ok(monthlyStats);
   }
}
