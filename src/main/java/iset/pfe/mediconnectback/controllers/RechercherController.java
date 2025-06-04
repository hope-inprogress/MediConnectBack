package iset.pfe.mediconnectback.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.dtos.MedecinDTO;
import iset.pfe.mediconnectback.services.FichierMedicalService;
import iset.pfe.mediconnectback.services.MedecinService;

@RestController
@RequestMapping("/api/rechercher")
@CrossOrigin(origins = "http://localhost:5173")
public class RechercherController {

    @Autowired
    private MedecinService medecinService;




}
