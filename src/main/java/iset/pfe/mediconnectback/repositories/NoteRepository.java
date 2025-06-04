package iset.pfe.mediconnectback.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("SELECT n FROM Note n WHERE n.user.id = :userId")
    List<Note> findByUserId(@Param("userId") Long userId);
}