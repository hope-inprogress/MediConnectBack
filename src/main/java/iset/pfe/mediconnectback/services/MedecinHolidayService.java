package iset.pfe.mediconnectback.services;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.MedecinHoliday;
import iset.pfe.mediconnectback.repositories.MedecinHolidayRepository;

@Service
public class MedecinHolidayService {

    @Autowired
    private MedecinHolidayRepository holidayRepository;

    @Autowired
    private MedecinService medecinService;

    public void addHoliday(Long medecinId, LocalDate date, String reason) {
        if (holidayRepository.existsByMedecinIdAndDate(medecinId, date)) {
            throw new IllegalStateException("Holiday already exists for this date.");
        }
        Medecin medecin = medecinService.getMedecinById(medecinId);
        MedecinHoliday holiday = new MedecinHoliday();
        holiday.setDate(date);
        holiday.setReason(reason);
        holiday.setMedecin(medecin);
        holidayRepository.save(holiday);
    }

    public void removeHoliday(Long medecinId, LocalDate date) {
        holidayRepository.deleteByMedecinIdAndDate(medecinId, date);
    }

    public List<MedecinHoliday> getHolidays(Long medecinId) {
        return holidayRepository.findByMedecinId(medecinId);
    }

    public boolean isHoliday(Long medecinId, LocalDate date) {
        return holidayRepository.existsByMedecinIdAndDate(medecinId, date);
    }
}

