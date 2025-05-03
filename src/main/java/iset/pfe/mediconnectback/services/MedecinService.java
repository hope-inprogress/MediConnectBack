package iset.pfe.mediconnectback.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.repositories.MedecinRepository;

@Service
public class MedecinService {
    
    @Autowired
    private MedecinRepository medecinRepository;
    
    
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
}
