package iset.pfe.mediconnectback.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iset.pfe.mediconnectback.dtos.MedecinDTO;
import iset.pfe.mediconnectback.dtos.PatientDTO;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Note;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.Specialite;
import iset.pfe.mediconnectback.enums.RendezVousType;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.repositories.NoteRepository;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.dtos.DocumentMedicalDto;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.services.UserService;

@Service
public class MedecinService {
    
    @Autowired
    private MedecinRepository medecinRepository;
    
    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RendezVousService rendezVousService;

    // get all medecins
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    // Get a specific medecin by ID
    public Medecin getMedecinById(Long medecinId) {
        return medecinRepository.findById(medecinId).orElseThrow(() -> new RuntimeException("Medecin not found with ID: " + medecinId));
    }

    // Get the count of medecins registered in each month of the current year
    public List<Integer> getMedecinsByMonth() {
        List<Integer> monthlyCounts = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            Long count = medecinRepository.countMedecinByMonth(month);
            monthlyCounts.add(count != null ? count.intValue() : 0); // Ensure no null values
        }
        
        return monthlyCounts;
    }

    // Get all appointments (rendezvous) for a specific medecin
    public List<RendeVousDTO> getAppointmentsByMedecin(Long medecinId) {
        List<RendezVous> appointments = rendezVousRepository.findByMedecinId(medecinId);
        return appointments.stream()
            .map(rendezVous -> rendezVousService.convertToDto(
                rendezVous,
                rendezVous.getRendezVousType() == RendezVousType.EnPersonne
                    ? rendezVous.getMedecin().getPriceInPerson()
                    : rendezVous.getMedecin().getPriceOnline()))
            .collect(Collectors.toList());
    }

    // Get the next appointment for a specific medecin
    public List<RendeVousDTO> getNextAppointmentsByMedecin(Long medecinId) {
        List<RendezVous> appointments = rendezVousRepository.findUpComingByMedecinId(medecinId);
        return appointments.stream()
            .map(rendezVous -> rendezVousService.convertToDto(
                rendezVous,
                rendezVous.getRendezVousType() == RendezVousType.EnPersonne
                    ? rendezVous.getMedecin().getPriceInPerson()
                    : rendezVous.getMedecin().getPriceOnline()))
            .collect(Collectors.toList());
    }


    public List<PatientDTO> getLatestPatientsByMedecin(Long medecinId) {
        List<Patient> patients = patientRepository.find5TopPatientsByMedecinId(medecinId);
        
        return patients.stream()
            .map(patient -> {
                PatientDTO dto = new PatientDTO();
                dto.setId(patient.getId());
                dto.setFirstName(patient.getFirstName());
                dto.setLastName(patient.getLastName());
                dto.setDateNaissance(patient.getDateNaissance());
                dto.setImageUrl(patient.getImageUrl());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedecinDTO> getMedecinsByPatient(Long patientId) {
        List<Medecin> medecins = medecinRepository.findByPatientId(patientId);
        
        // Convert Patient entities to PatientDTOs
        return medecins.stream()
                .map(this::convertToMedecinDTO)
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
        dto.setTypeRendezVous(medecin.getRendezVousType() != null ? medecin.getRendezVousType().toString() : null);
        dto.setUserStatus(medecin.getUserStatus());
        dto.setWorkDays(medecin.getWorkingDays().stream().map(DayOfWeek::toString).collect(Collectors.toSet()));
        dto.setStartTime(medecin.getStartTime().toString());
        dto.setEndTime(medecin.getEndTime().toString());
        
        return dto;
    }

    @Transactional
    public void addToPatient(Long patientId, Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
            .orElseThrow(() -> new RuntimeException("Medecin not found"));
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Add patient to doctor's list if not already there
        if (!medecin.getMesPatients().contains(patient)) {
            medecin.getMesPatients().add(patient);
            medecinRepository.save(medecin);
        }

        // Add doctor to patient's list if not already there
        if (!patient.getMesMedecins().contains(medecin)) {
            patient.getMesMedecins().add(medecin);
            patientRepository.save(patient);
        }
    }

    @Transactional
    public void removePatientFromMedecin(Long medecinId, Long patientId) {
        Medecin medecin = medecinRepository.findById(medecinId)
            .orElseThrow(() -> new RuntimeException("Medecin not found"));
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Remove patient from doctor's list
        medecin.getMesPatients().remove(patient);
        medecinRepository.save(medecin);

        // Remove doctor from patient's list
        patient.getMesMedecins().remove(medecin);
        patientRepository.save(patient);
    }

    @Transactional
    public void updateWorkDays(Long medecinId, Set<DayOfWeek> workDays) {
        Medecin medecin = medecinRepository.findById(medecinId)
            .orElseThrow(() -> new RuntimeException("Medecin not found"));

        medecin.setWorkingDays(workDays);
    }








                // if today the medecin is on holiday or today is not a working day, set the isAvailable to false
            @Transactional
            // check Every day
            @Scheduled(fixedRate = 86400 * 1000) 
            void updateMedecinAvailability() {
                LocalDate today = LocalDate.now();
                List<Medecin> medecins = medecinRepository.findAll();
                for (Medecin medecin : medecins) {

                   // if it's working day and the medecin is not on holiday, set the isAvailable to true
                    if (medecin.getHolidays() != null && medecin.getHolidays().contains(today)) {
                        medecin.setIsAvailable(false);
                    } else if (isWorkingDay(medecin, today)) {
                        medecin.setIsAvailable(true);
                    } else {
                        medecin.setIsAvailable(false);
                    }
                    medecinRepository.save(medecin);
                    
                }
            }
    private boolean isWorkingDay(Medecin medecin, LocalDate date) {
        // Check if the date is a working day for the medecin
        if (medecin.getWorkingDays() == null || medecin.getWorkingDays().isEmpty()) {
            return false; // No working days set, consider it a non-working day
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return medecin.getWorkingDays().contains(dayOfWeek);
    }

}
