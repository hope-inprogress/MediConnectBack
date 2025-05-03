package iset.pfe.mediconnectback.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.services.MedecinService;

@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/medecins")
public class MedecinController {
    
    @Autowired
    private MedecinService medecinService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Medecin> getMedecins() {
        return medecinService.getAllMedecins();
 
    }

    @PreAuthorize("hasRole('ADMIN')")
   @GetMapping("/monthly")
     public ResponseEntity<List<Integer>> getMonthlyPatientStats() {
        List<Integer> monthlyStats = medecinService.getMedecinsByMonth();
        return ResponseEntity.ok(monthlyStats);
    }


}
