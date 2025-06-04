package iset.pfe.mediconnectback.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.MedecinHoliday;

@Repository
public interface MedecinHolidayRepository extends JpaRepository<MedecinHoliday, Long> {
    List<MedecinHoliday> findByMedecinId(Long medecinId);
    boolean existsByMedecinIdAndDate(Long medecinId, LocalDate date);
    void deleteByMedecinIdAndDate(Long medecinId, LocalDate date);
}

