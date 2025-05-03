package iset.pfe.mediconnectback.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.repositories.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Integer> getPatientsByMonth() {
        List<Integer> monthlyCounts = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            Long count = patientRepository.countPatientsByMonth(month);
            monthlyCounts.add(count != null ? count.intValue() : 0); // Ensure no null values
        }
        
        return monthlyCounts;
    }
}
