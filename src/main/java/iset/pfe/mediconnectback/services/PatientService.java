package iset.pfe.mediconnectback.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import iset.pfe.mediconnectback.dtos.DocumentMedicalDto;
import iset.pfe.mediconnectback.dtos.DossierMedicalDTO;
import iset.pfe.mediconnectback.dtos.PatientDTO;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousType;
import iset.pfe.mediconnectback.enums.Specialite;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.dtos.MedecinDTO;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    @Lazy
    private RendezVousService rendezVousService;

    @Autowired
    private MedecinRepository medecinRepository;


    public List<Integer> getPatientsByMonth() {
        List<Integer> monthlyCounts = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            Long count = patientRepository.countPatientsByMonth(month);
            monthlyCounts.add(count != null ? count.intValue() : 0); // Ensure no null values
        }
        
        return monthlyCounts;
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

        // Get all patients associated with a specific medecin, including their DossierMedical and fichiers
    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByMedecin(Long medecinId) {
        User currentUser = userService.findById(medecinId);
        List<Patient> patients = patientRepository.findPatientsByMedecinId(medecinId);
        
        // Convert Patient entities to PatientDTOs
        return patients.stream()
                .map(patient -> convertToPatientDTO(patient, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Integer countActivePatientsByMedecinId(Long medecinId) {
        List<PatientDTO> patients = getPatientsByMedecin(medecinId);
        List<PatientDTO> activePatients = new ArrayList<>();
        for (PatientDTO patient : patients) {
            if (patient.getUserStatus() == UserStatus.Active) {
                activePatients.add(patient);
            }
        }
        return activePatients.size();
    }

    private PatientDTO convertToPatientDTO(Patient patient, User currentUser) {
        PatientDTO dto = new PatientDTO();

        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setAddress(patient.getAddress());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setDateNaissance(patient.getDateNaissance());
        dto.setSexe(patient.getSexe() != null ? patient.getSexe().toString() : null);
        dto.setImageUrl(patient.getImageUrl());
        dto.setUserStatus(patient.getUserStatus());
             // Fetch and map DossierMedical and its fichiers
        DossierMedical dossierMedical = dossierMedicalRepository.findByPatientIdWithFichiers(patient.getId());

        if (dossierMedical != null) {
            DossierMedicalDTO dossierMedicalDTO = new DossierMedicalDTO();
            dossierMedicalDTO.setId(dossierMedical.getId());
            dossierMedicalDTO.setDateCreated(dossierMedical.getDateCreated());

            // Map the list of DocumentMedical entities to DocumentMedicalDTOs
            List<DocumentMedicalDto> fichierDTOs = dossierMedical.getFichiers().stream()
                    .map(doc -> convertToDocumentMedicalDTO(doc, currentUser))
                    .collect(Collectors.toList());

            dossierMedicalDTO.setFichiers(fichierDTOs);
            dto.setDossierMedical(dossierMedicalDTO);
        }
        
        return dto;
    }

    private DocumentMedicalDto convertToDocumentMedicalDTO(DocumentMedical document, User currentUser) {
        DocumentMedicalDto dto = new DocumentMedicalDto();
        dto.setId(document.getId());
        dto.setType(document.getType());
        dto.setFichier(document.getFichier());
        dto.setUploadDate(document.getCreatedAt());
        dto.setVisibility(document.getVisibility().name());
        
        // Set uploader name and image based on current user comparison
        if (document.getUploader().getId().equals(currentUser.getId())) {
            dto.setUploaderName("Vous");
        } else {
            dto.setUploaderName(document.getUploader().getFirstName() + " " + document.getUploader().getLastName());
            dto.setUploaderImage(document.getUploader().getImageUrl());
        }
        
        return dto;
    }

        // Get all appointments (rendezvous) for a specific Patient
    public List<RendeVousDTO> getAppointmentsByPatient(Long patientId) {

        // Fetch and return the list of RendezVous (appointments) for this patient
        List<RendezVous> RendezVouss = rendezVousRepository.findByPatientId(patientId);

        return RendezVouss.stream()
            .map(rendezVous -> rendezVousService.convertToDto(
                rendezVous,
                rendezVous.getRendezVousType() == RendezVousType.EnPersonne
                    ? rendezVous.getMedecin().getPriceInPerson()
                    : rendezVous.getMedecin().getPriceOnline()))
            .collect(Collectors.toList());
    }

    // Get the next appointment for a specific patient
    public List<RendeVousDTO> getNextAppointmentsByPatient(Long patientId) {
        // Fetch and return the next appointment for this patient
        List<RendezVous> upcomingRendezVous =  rendezVousRepository.findUpComingByPatientId(patientId);

        return upcomingRendezVous.stream()
            .map(rendezVous -> rendezVousService.convertToDto(
                rendezVous,
                rendezVous.getRendezVousType() == RendezVousType.EnPersonne
                    ? rendezVous.getMedecin().getPriceInPerson()
                    : rendezVous.getMedecin().getPriceOnline()))
            .collect(Collectors.toList());
    }

    private MedecinDTO convertToMedecinDTO(Medecin medecin) {
        MedecinDTO dto = new MedecinDTO();
        dto.setId(medecin.getId());
        dto.setFirstName(medecin.getFirstName());
        dto.setLastName(medecin.getLastName());
        dto.setEmail(medecin.getEmail());
        dto.setAddress(medecin.getAddress());
        dto.setPhoneNumber(medecin.getPhoneNumber());
        dto.setDateNaissance(medecin.getDateNaissance());
        dto.setCodeMedical(medecin.getCodeMedical());
        dto.setSpecialitePrimaire(medecin.getSpecialitePrimaire() != null ? medecin.getSpecialitePrimaire().toString() : null);
        dto.setSpecialiteSecondaire(medecin.getSpecialiteSecondaire() != null ? medecin.getSpecialiteSecondaire().stream().map(Specialite::toString).collect(Collectors.toList()) : null);
        dto.setDescription(medecin.getDescription());
        dto.setIsAvailable(medecin.getIsAvailable());
        dto.setSexe(medecin.getSexe() != null ? medecin.getSexe().toString() : null);
        dto.setImageUrl(medecin.getImageUrl());
        dto.setWorkPlace(medecin.getWorkPlace());
        dto.setTypeRendezVous(null != medecin.getRendezVousType() ? medecin.getRendezVousType().toString() : null);
        
        dto.setUserStatus(medecin.getUserStatus());
        return dto;
    }

    @Transactional
    public void addFavoriteMedecin(Long patientId, Long medecinId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        Medecin medecin = medecinRepository.findById(medecinId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medecin not found"));

        if (!patient.getFavoriteMedecins().contains(medecin)) {
            patient.getFavoriteMedecins().add(medecin);
            patientRepository.save(patient);
        }
    }

    @Transactional(readOnly = true)
    public List<MedecinDTO> getFavoriteMedecins(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        Set<Medecin> favoriteMedecins = patient.getFavoriteMedecins();
        return favoriteMedecins.stream()
            .map(this::convertToMedecinDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromFavorites(Long patientId, Long medecinId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        Medecin medecin = medecinRepository.findById(medecinId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medecin not found"));
        if (patient.getFavoriteMedecins().contains(medecin)) {
            patient.getFavoriteMedecins().remove(medecin);
            patientRepository.save(patient);
        }
    }

    @Transactional(readOnly = true)
    public int countFavorites(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return patient.getFavoriteMedecins().size();
    }


}


