package iset.pfe.mediconnectback.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;
import jakarta.transaction.Transactional;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.entities.Medecin;

@Service
public class AppointmentScheduler {

    @Autowired
    private RendezVousRepository appointmentRepository;

    private final Object lock = new Object();

    // Process Pending and Reschedule_Requested appointments (auto-confirm or reject)
    @Transactional
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void processAppointments() {
        synchronized (lock) {
            LocalDateTime now = LocalDateTime.now();
            List<RendezVous> toUpdate = new ArrayList<>();
    
            // Process Pending appointments
            List<RendezVous> pendingAppointments = new ArrayList<>(appointmentRepository.findByRendezVousStatut(RendezVousStatut.Pending)); // Make a copy
            for (RendezVous appt : pendingAppointments) {
                Medecin medecin = appt.getMedecin();
                LocalDateTime appointmentDateTime = LocalDateTime.of(appt.getAppointmentDate(), appt.getAppointmentTime());
    
                if (medecin.isAutoManageAppointments()) {
                    String validationResult = isAppointmentValid(appt, medecin, appointmentDateTime);
                    if (validationResult == null) {
                        appt.setRendezVousStatut(RendezVousStatut.Confirmed);
                        appt.setErrorMessage(null);
                    } else if (appt.getCreatedAt().plusHours(48).isBefore(now)) {
                        appt.setRendezVousStatut(RendezVousStatut.Rejected);
                        appt.setErrorMessage("Rendez-vous auto rejeté : invalide après 48 heures.");
                    } else {
                        appt.setErrorMessage(validationResult);
                    }
                } else {
                    if (appt.getCreatedAt().plusHours(48).isBefore(now)) {
                        appt.setRendezVousStatut(RendezVousStatut.Rejected);
                        appt.setErrorMessage("Rendez-vous non confirmé manuellement dans les 48 heures.");
                    }
                }
                toUpdate.add(appt);
            }
    
            // Process Reschedule_Requested appointments
            List<RendezVous> rescheduleRequests = new ArrayList<>(appointmentRepository.findByRendezVousStatut(RendezVousStatut.Reschedule_Requested)); // Make a copy
            for (RendezVous appt : rescheduleRequests) {
                Medecin medecin = appt.getMedecin();
                LocalDateTime appointmentDateTime = LocalDateTime.of(appt.getAppointmentDate(), appt.getAppointmentTime());
    
                if (medecin.isAutoManageAppointments()) {
                    String validationResult = isAppointmentValid(appt, medecin, appointmentDateTime);
                    if (validationResult == null) {
                        appt.setRendezVousStatut(RendezVousStatut.Rescheduled);
                        appt.setErrorMessage(null);
                    } else if (appt.getRescheduleRequestTime() != null && appt.getRescheduleRequestTime().plusHours(48).isBefore(now)) {
                        appt.setRendezVousStatut(RendezVousStatut.Rejected);
                        appt.setErrorMessage("Demande de reprogrammation auto rejetée : invalide après 48 heures.");
                    } else {
                        appt.setErrorMessage(validationResult);
                    }
                } else {
                    if (appt.getRescheduleRequestTime() != null && appt.getRescheduleRequestTime().plusHours(48).isBefore(now)) {
                        appt.setRendezVousStatut(RendezVousStatut.Rejected);
                        appt.setErrorMessage("Demande de reprogrammation non traitée dans les 48 heures.");
                    }
                }
                toUpdate.add(appt);
            }
    
            if (!toUpdate.isEmpty()) {
                appointmentRepository.saveAll(toUpdate);
            }
        }
    }
    

    // Automatically complete past confirmed/rescheduled appointments
    @Transactional
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void autoCompletePastAppointments() {
        synchronized (lock) {
            LocalDateTime now = LocalDateTime.now();
            List<RendezVous> toUpdate = new ArrayList<>();

            List<RendezVous> activeAppointments = appointmentRepository.findByRendezVousStatutIn(
                List.of(RendezVousStatut.Confirmed, RendezVousStatut.Rescheduled));

            for (RendezVous appt : activeAppointments) {
                LocalDateTime appointmentDateTime = LocalDateTime.of(appt.getAppointmentDate(), appt.getAppointmentTime());
                if (appointmentDateTime.isBefore(now.minusMinutes(30))) {
                    appt.setRendezVousStatut(RendezVousStatut.Completed);
                    appt.setErrorMessage(null);
                    toUpdate.add(appt);
                }
            }

            if (!toUpdate.isEmpty()) {
                appointmentRepository.saveAll(toUpdate);
            }
        }
    }

    // Helper: Validate time/date/availability of appointment
    private String isAppointmentValid(RendezVous appt, Medecin medecin, LocalDateTime appointmentDateTime) {
        if (medecin == null) {
            return "Aucun médecin associé à ce rendez-vous.";
        }

        if (!medecin.getIsAvailable()) {
            return "Le médecin n'est pas disponible.";
        }

        if (medecin.getStartTime() == null || medecin.getEndTime() == null) {
            return "Les heures de travail du médecin ne sont pas définies.";
        }

        LocalTime appointmentTime = appt.getAppointmentTime();
        if (appointmentTime.isBefore(medecin.getStartTime()) || appointmentTime.isAfter(medecin.getEndTime())) {
            return "L'heure du rendez-vous est en dehors des heures de travail du médecin.";
        }

        List<RendezVous> existingAppointments = appointmentRepository
            .findByMedecinIdAndAppointmentDateAndRendezVousStatutIn(
                medecin.getId(), appt.getAppointmentDate(), 
                List.of(RendezVousStatut.Confirmed, RendezVousStatut.Rescheduled)
            );

        for (RendezVous existingAppt : existingAppointments) {
            LocalDateTime existingDateTime = LocalDateTime.of(
                existingAppt.getAppointmentDate(), existingAppt.getAppointmentTime());

            if (Duration.between(existingDateTime, appointmentDateTime).abs().toMinutes() < 30) {
                return "Le créneau horaire est déjà réservé.";
            }
        }

        return null; // Valid
    }
}
