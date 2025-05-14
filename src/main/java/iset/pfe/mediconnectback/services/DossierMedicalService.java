package iset.pfe.mediconnectback.services;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;
import jakarta.transaction.Transactional;

@Service
public class DossierMedicalService {

    @Autowired
    private MedecinService medecinServcie;

    @Transactional
    public List<DossierMedical> getDossiersForMedecin(Long medecinId) {
    Medecin medecin = medecinServcie.getMedecinById(medecinId);

    return medecin.getMesPatients().stream()
            .map(Patient::getDossierMedical)
            .filter(Objects::nonNull)
            .toList();
    }

}
