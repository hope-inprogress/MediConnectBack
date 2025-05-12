package iset.pfe.mediconnectback.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.services.DossierMedicalService;
import iset.pfe.mediconnectback.services.JwtService;

@RestController
@RequestMapping("/api/dossiers")
@CrossOrigin(origins = "http://localhost:5173")
public class DossierMedicalController {

    @Autowired
    private DossierMedicalService dossierService;

    @Autowired
    private JwtService jwtService;
    
    // Get all dossiers for a specific patient
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping
    public ResponseEntity<List<DossierMedical>> getAllDossiersForMedecin(@RequestHeader("Authorization") String token) {
        Long medecinId = jwtService.extractIdFromBearer(token);
        List<DossierMedical> dossiers = dossierService.getDossiersForMedecin(medecinId);
        return ResponseEntity.ok(dossiers);
    }




}
