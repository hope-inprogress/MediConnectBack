package iset.pfe.mediconnectback.services;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.DocumentMedical;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Note;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
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
    public List<Patient> getPatientsByMedecin(Long medecinId) {
                    // If the Medecin is not found, return 404        // Fetch patients with their DossierMedical and fichiers in a single query
        return rendezVousRepository.findDistinctPatientsByMedecinIdWithDossierMedical(medecinId);
    }

    // Get all appointments (rendezvous) for a specific medecin
    public List<RendezVous> getAppointmentsByMedecin(Long medecinId) {
        // Fetch and return the list of RendezVous (appointments) for this Medecin
        return rendezVousRepository.findByMedecinId(medecinId);
    }


    // Retrieve all DossierMedical records that belong to patients linked with this medecin
    public List<DossierMedical> getDossierMedical(Long medecinId) {
        return dossierMedicalRepository.findByMedecinId(medecinId);
    }


    // Retrieve all documents in a dossier, including the ones added by other medecins
    public List<DocumentMedical> getDocumentsByDossier(Long dossierId) {
        return documentMedicalRepository.findByDossierMedicalId(dossierId);
    }

     // Delete a document if uploaded by this medecin
     public void deleteDocument(Long docId, Long medecinId) {
        DocumentMedical doc = documentMedicalRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getMedecin().getId().equals(medecinId)) {
            throw new RuntimeException("Not authorized to delete this document");
        }
        documentMedicalRepository.delete(doc);
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

    public void updateAppointmentStatus(Long appointmentId, String status, Long medecinId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAppointmentStatus'");
    }


    // Get all appointments along with their statuses (completed, cancelled, no-show, etc.)
   /*  public List<RendezVous> getAllRendezVousWithStatus(Long medecinId) {
    }*/

    
}
