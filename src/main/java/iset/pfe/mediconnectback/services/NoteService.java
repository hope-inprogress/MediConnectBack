package iset.pfe.mediconnectback.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Note;
import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.repositories.NoteRepository;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserService userService;

    // Get all private notes for this user
    public List<Note> getPrivateNotes(Long userId) {
        return noteRepository.findByUserId(userId);
    }

    // Add a private note for this user only
    public void addPrivateNote(Long userId, Note note) {
        User user = userService.findById(userId);
        note.setUser(user);
        noteRepository.save(note);
    }

    // Update a private note (only the creator can update it)
    public Note updateNote(Long noteId, Long userId, Note updatedNote) {
        Note existingNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        if (!existingNote.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this note.");
        }

        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setContent(updatedNote.getContent());
        existingNote.setDateAjout(updatedNote.getDateAjout());

        return noteRepository.save(existingNote);
    }


        // Delete a private note (only the medecin who created it can delete it)
    public void deletePrivateNote(Long medecinId, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        // Ensure the medecinId matches the medecin who created the note
        if (!note.getUser().getId().equals(medecinId)) {
            throw new RuntimeException("Not authorized to delete this note");
        }

        noteRepository.delete(note);
    }

}
