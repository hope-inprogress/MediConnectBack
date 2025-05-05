package iset.pfe.mediconnectback.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.repositories.MotifsRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;

@Service
public class MedecinService {
    
    @Autowired
    private MedecinRepository medecinRepository;
    
    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MotifsRepository motifsRepository;
    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    private String uploadDir;
    
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    public List<Integer> getMedecinsByMonth() {
        List<Integer> monthlyCounts = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            Long count = medecinRepository.countMedecinByMonth(month);
            monthlyCounts.add(count != null ? count.intValue() : 0); // Ensure no null values
        }
        
        return monthlyCounts;
    }

    public List<Patient> getPatientsByMedecin(Long medecinId) {
        // Fetch the doctor
       medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Fetch patients with their DossierMedical and fichiers in a single query
        return rendezVousRepository.findDistinctPatientsByMedecinIdWithDossierMedical(medecinId);
    }

    
}
