package iset.pfe.mediconnectback.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.dtos.MedecinResponse;
import iset.pfe.mediconnectback.dtos.PatientResponse;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;
import jakarta.transaction.Transactional;

@Service
public class RendezVousService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Transactional
    public RendezVous updateRendezvousStatus(Long id, RendezVousStatut newStatus) {
        RendezVous rendezvous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rendezvous with ID " + id + " not found"));

        RendezVousStatut currentStatus = rendezvous.getRendezVousStatut();
        Medecin medecin = rendezvous.getMedecin();
        LocalTime appointmentTime = rendezvous.getAppointmentTime();

        boolean isBooked = rendezVousRepository.existsByMedecinAndAppointmentTime(medecin, appointmentTime);

        switch (newStatus) {
            case Confirmed:
                if (!currentStatus.equals(RendezVousStatut.Pending)) {
                    throw new IllegalStateException("Can only confirm a pending appointment");
                }
                if (!medecin.getIsAvailable()) {
                    throw new IllegalStateException("Doctor is not available");
                }
                if (isBooked) {
                    throw new IllegalStateException("This time slot is already booked");
                }
                rendezvous.setRendezVousStatut(RendezVousStatut.Confirmed);
                rendezvous.setCreatedAt(LocalDateTime.now());
                break;

            case Cancelled:
                if (currentStatus.equals(RendezVousStatut.Confirmed)) {
                    rendezvous.setCancelledAt(LocalDateTime.now());
                    rendezvous.setRendezVousStatut(RendezVousStatut.Cancelled);
                }
                break;

            case Completed:
                if (!currentStatus.equals(RendezVousStatut.Confirmed)) {
                    throw new IllegalStateException("Can only complete a confirmed appointment");
                }
                rendezvous.setCompletedAt(LocalDateTime.now());
                rendezvous.setRendezVousStatut(RendezVousStatut.Completed);
                break;

            default:
                throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        return rendezVousRepository.save(rendezvous);
    }

    public List<RendeVousDTO> getAllRendezVous() {
        List<RendezVous> rendezVousList = rendezVousRepository.findAll();
        
        return rendezVousList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RendeVousDTO convertToDto(RendezVous rendezVous) {
        RendeVousDTO dto = new RendeVousDTO();
        dto.setId(rendezVous.getId());
        dto.setAppointmentTime(rendezVous.getAppointmentTime());
        dto.setAppointmentDate(rendezVous.getAppointmentDate());
        dto.setReason(rendezVous.getReason());
        dto.setRendezVousStatut(rendezVous.getRendezVousStatut());
        dto.setCreatedAt(rendezVous.getCreatedAt());
        
        // Map Patient
        if (rendezVous.getPatient() != null) {
            Patient patient = rendezVous.getPatient();
            PatientResponse patientResponse = new PatientResponse();
            patientResponse.setFirstName(patient.getFirstName());
            patientResponse.setLastName(patient.getLastName());
            patientResponse.setEmail(patient.getEmail());
            patientResponse.setAddress(patient.getAddress());
            patientResponse.setImageUrl(patient.getImageUrl());
            patientResponse.setAccountStatus(patient.getAccountStatus() != null ? patient.getAccountStatus().name() : null);
            patientResponse.setPhoneNumber(patient.getPhoneNumber());
            patientResponse.setDateNaissance(patient.getDateNaissance());
            patientResponse.setSexe(patient.getSexe() != null ? patient.getSexe().name() : null);
            dto.setPatient(patientResponse);
        }
        
        // Map Medecin
        if (rendezVous.getMedecin() != null) {
            Medecin medecin = rendezVous.getMedecin();
            MedecinResponse medecinResponse = new MedecinResponse();
            medecinResponse.setFirstName(medecin.getFirstName());
            medecinResponse.setLastName(medecin.getLastName());
            medecinResponse.setEmail(medecin.getEmail());
            medecinResponse.setAddress(medecin.getAddress());
            medecinResponse.setImageUrl(medecin.getImageUrl());
            medecinResponse.setAccountStatus(medecin.getAccountStatus() != null ? medecin.getAccountStatus().name() : null);
            medecinResponse.setCodeMedical(medecin.getCodeMedical());
            medecinResponse.setPhoneNumber(medecin.getPhoneNumber());
            medecinResponse.setWorkPlace(medecin.getWorkPlace());
            medecinResponse.setStartTime(medecin.getStartTime());
            medecinResponse.setEndTime(medecin.getEndTime());
            medecinResponse.setIsAvailable(medecin.getIsAvailable());
            medecinResponse.setStartingPrice(medecin.getStartingPrice());
            medecinResponse.setDescription(medecin.getDescription());
            dto.setMedecin(medecinResponse);
        }
        
        return dto;
    }

    public Map<String, Long> getRendezVousStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("Confirmé", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Confirmed));
        stats.put("Annulé", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Cancelled));
        stats.put("Rejeté", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Rejected));
        stats.put("En_attente", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Pending));
        stats.put("Reprogrammé", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Rescheduled));
        stats.put("Reprogrammer_Demande", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Reschedule_Requested));
        stats.put("Terminé", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.Completed));
        stats.put("Pas_de_présentation", rendezVousRepository.countByRendezVousStatut(RendezVousStatut.No_Show));
        stats.put("Total", rendezVousRepository.count());
        return stats;
    }

    @Transactional
    public RendezVous updateStatusManually(Long appointmentId, RendezVousStatut newStatus, String errorMessage, Long doctorId) {
        RendezVous appointment = rendezVousRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));

        if (appointment.getMedecin() == null) {
            throw new IllegalStateException("Médecin non associé au rendez-vous.");
        }

        // Verify the doctor matches
        if (!appointment.getMedecin().getId().equals(doctorId)) {
            throw new SecurityException("Non autorisé : vous n'êtes pas le médecin de ce rendez-vous.");
        }

        // Reject if doctor is in auto-mode
        if (appointment.getMedecin().isAutoManageAppointments()) {
            throw new IllegalStateException("Manual updates disabled in auto-mode.");
        }

        // Validate status transition
        if (!isValidTransition(appointment.getRendezVousStatut(), newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + appointment.getRendezVousStatut() + " to " + newStatus);
        }

        // Update status
        appointment.setRendezVousStatut(newStatus);
        if (newStatus == RendezVousStatut.Rejected || newStatus == RendezVousStatut.Cancelled) {
            appointment.setErrorMessage(
                    errorMessage != null ? errorMessage : "Rendez-vous " + newStatus.name().toLowerCase() + " manuellement.");
        } else {
            appointment.setErrorMessage(null);
        }

        return rendezVousRepository.save(appointment);
    }

    private boolean isValidTransition(RendezVousStatut current, RendezVousStatut newStatus) {
        if (current == newStatus) return false;
    
        switch (current) {
            case Pending:
                return newStatus == RendezVousStatut.Confirmed ||
                       newStatus == RendezVousStatut.Reschedule_Requested ||
                       newStatus == RendezVousStatut.Rejected ||
                       newStatus == RendezVousStatut.Cancelled;
    
            case Reschedule_Requested:
                return newStatus == RendezVousStatut.Confirmed ||
                       newStatus == RendezVousStatut.Rescheduled ||
                       newStatus == RendezVousStatut.Rejected ||
                       newStatus == RendezVousStatut.Cancelled;
    
            case Confirmed:
                return newStatus == RendezVousStatut.Completed ||
                       newStatus == RendezVousStatut.Cancelled ||
                       newStatus == RendezVousStatut.Reschedule_Requested ||
                       newStatus == RendezVousStatut.No_Show;
    
            case Rescheduled:
                return newStatus == RendezVousStatut.Completed ||
                       newStatus == RendezVousStatut.Cancelled ||
                       newStatus == RendezVousStatut.No_Show;
    
            case No_Show:
                return newStatus == RendezVousStatut.Reschedule_Requested;
    
            case Completed:
            case Cancelled:
            case Rejected:
                return false;
    
            default:
                throw new IllegalStateException("Unknown status: " + current);
        }
    }
}
