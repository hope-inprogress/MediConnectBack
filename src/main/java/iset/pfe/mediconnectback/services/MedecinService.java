package iset.pfe.mediconnectback.services;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iset.pfe.mediconnectback.dtos.DocumentMedicalDto;
import iset.pfe.mediconnectback.dtos.DossierMedicalDTO;
import iset.pfe.mediconnectback.dtos.PatientDTO;
import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Note;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.repositories.DocumentMedicalRepository;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.repositories.NoteRepository;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;

@Service
public class MedecinService {
    
    @Autowired
    private MedecinRepository medecinRepository;
    
    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private DocumentMedicalRepository documentMedicalRepository;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    @Autowired
    private NoteRepository noteRepository;

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

    // Get all patients associated with a specific medecin, including their DossierMedical and fichiers
    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByMedecin(Long medecinId) {
        List<Patient> patients = rendezVousRepository.findDistinctPatientsByMedecinIdWithDossierMedical(medecinId);
        
        // Convert Patient entities to PatientDTOs
        return patients.stream()
                .map(this::convertToPatientDTO)
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

    private PatientDTO convertToPatientDTO(Patient patient) {
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
                    .map(this::convertToDocumentMedicalDTO)
                    .collect(Collectors.toList());

            dossierMedicalDTO.setFichiers(fichierDTOs);
            dto.setDossierMedical(dossierMedicalDTO);
        }
        
        return dto;
    }

    private DocumentMedicalDto convertToDocumentMedicalDTO(DocumentMedical document) {
        DocumentMedicalDto dto = new DocumentMedicalDto();
        dto.setId(document.getId());
        dto.setType(document.getType());
        dto.setFichier(document.getFichier()); // This is your file name/path/URL
        dto.setUploadDate(document.getCreatedAt());
        dto.setUploaderId(document.getUploader().getId());
        dto.setVisibility(document.getVisibility().name());
        return dto;
    }

    // Get all appointments (rendezvous) for a specific medecin
    public List<RendezVous> getAppointmentsByMedecin(Long medecinId) {
        // Fetch and return the list of RendezVous (appointments) for this Medecin
        return rendezVousRepository.findByMedecinId(medecinId);
    }

    // Get the next appointment for a specific medecin
    public List<RendezVous> getNextAppointmentsByMedecin(Long medecinId) {
        // Fetch and return the next appointment for this Medecin
        return rendezVousRepository.findUpcomingByMedecinId(medecinId);
    }

    // Add a private note for this medecin only
    public void addPrivateNote(Long medecinId, Note note) {
        Medecin medecin = getMedecinById(medecinId);
        note.setMedecin(medecin);
        noteRepository.save(note);
    }

    // Get all private notes for this medecin
    public List<Note> getPrivateNotes(Long medecinId) {
        return noteRepository.findByMedecinId(medecinId);
    }

    // Delete a private note (only the medecin who created it can delete it)
    public void deletePrivateNote(Long medecinId, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Ensure the medecinId matches the medecin who created the note
        if (!note.getMedecin().getId().equals(medecinId)) {
            throw new RuntimeException("Not authorized to delete this note");
        }

        noteRepository.delete(note);
    }

    public Note updateNote(Long noteId, Long medecinId, Note updatedNote) {
        return noteRepository.findById(noteId).map(note -> {
            if (!note.getMedecin().getId().equals(medecinId)) {
                throw new RuntimeException("Unauthorized to update this note.");
            }
            note.setTitle(updatedNote.getTitle());
            note.setContent(updatedNote.getContent());
            note.setDateAjout(updatedNote.getDateAjout());
            return noteRepository.save(note);
        }).orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));
    }

    public List<PatientDTO> getLatestPatientsByMedecin(Long medecinId) {
        Pageable pageable = PageRequest.of(0, 5); // Get the latest 5 patients
        List<RendezVous> rendezVousList = rendezVousRepository.findTop5LatestRendezVousByMedecinId(medecinId, pageable);

        List<PatientDTO> patientDTOs = new ArrayList<>();

        for (RendezVous rv : rendezVousList) {
            Patient patient = rv.getPatient();

            PatientDTO dto = new PatientDTO();
            dto.setId(patient.getId());
            dto.setFirstName(patient.getFirstName());
            dto.setLastName(patient.getLastName());
            dto.setDateNaissance(patient.getDateNaissance());
            dto.setRendezVousCreatedDate(rv.getCreatedAt()); // You need to add this field in PatientDTO

            patientDTOs.add(dto);
        }

        return patientDTOs;
    }


}
