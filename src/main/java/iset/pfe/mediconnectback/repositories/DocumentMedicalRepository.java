package iset.pfe.mediconnectback.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.DocumentMedical;

@Repository
public interface DocumentMedicalRepository extends JpaRepository<DocumentMedical, Long> {
}
