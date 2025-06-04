package iset.pfe.mediconnectback.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.dtos.LocalDateTimeDTO;
import iset.pfe.mediconnectback.dtos.MedecinResponse;
import iset.pfe.mediconnectback.dtos.PatientResponse;
import iset.pfe.mediconnectback.dtos.RendeVousDTO;
import iset.pfe.mediconnectback.dtos.RendezVousRequestDTO;
import iset.pfe.mediconnectback.dtos.ReschedelRequestDTO;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.MedecinHoliday;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.entities.RendezVous;
import iset.pfe.mediconnectback.enums.RendezVousStatut;
import iset.pfe.mediconnectback.enums.RendezVousType;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;
import jakarta.transaction.Transactional;

@Service
public class RendezVousService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    @Lazy
    private MedecinService medecinService;

    @Autowired
    private PatientService patientService;

    public List<RendeVousDTO> getAllRendezVous() {
        List<RendezVous> rendezVousList = rendezVousRepository.findAll();
        
        return rendezVousList.stream()
        // Pass the price from medecin based on the type of rendezVous
                .map(rendezVous -> convertToDto(rendezVous, rendezVous.getRendezVousType() == RendezVousType.EnPersonne 
                        ? rendezVous.getMedecin().getPriceInPerson() 
                        : rendezVous.getMedecin().getPriceOnline()))
                .collect(Collectors.toList());
    }

    public RendeVousDTO convertToDto(RendezVous rendezVous, Long appointementPrice) {
        RendeVousDTO dto = new RendeVousDTO();
        dto.setId(rendezVous.getId());
        dto.setAppointmentTime(rendezVous.getAppointmentTime());
        dto.setAppointmentDate(rendezVous.getAppointmentDate());
        dto.setReason(rendezVous.getReason());
        dto.setRendezVousStatut(rendezVous.getRendezVousStatut());
        dto.setCreatedAt(rendezVous.getCreatedAt());
        dto.setAppointementPrice(appointementPrice);
        
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
            medecinResponse.setSpecialitePrimaire(medecin.getSpecialitePrimaire());
            medecinResponse.setSpecialiteSecondaire(medecin.getSpecialiteSecondaire());
            medecinResponse.setTypeRendezVous(medecin.getRendezVousType() != null ? medecin.getRendezVousType().toString() : null);
            medecinResponse.setPriceInPerson(medecin.getPriceInPerson());
            medecinResponse.setPriceOnline(medecin.getPriceOnline());
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
            if (newStatus == RendezVousStatut.Completed) {
                medecinService.addToPatient(doctorId, appointment.getPatient().getId());
            }
            appointment.setErrorMessage(null); 
        }

        return rendezVousRepository.save(appointment);
    }

    public RendeVousDTO rescheduleRequestAppointment(Long patientId, Long appointmentId, ReschedelRequestDTO dto) {
        RendezVous rendezVous = rendezVousRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));
        if (!rendezVous.getPatient().getId().equals(patientId)) {
            throw new SecurityException("Non autorisé : vous n'êtes pas le patient de ce rendez-vous.");
        }
        if (rendezVous.getRendezVousStatut() == RendezVousStatut.Completed ||
            rendezVous.getRendezVousStatut() == RendezVousStatut.Cancelled ||
            rendezVous.getRendezVousStatut() == RendezVousStatut.Rejected) {
            throw new IllegalStateException("Impossible de reprogrammer un rendez-vous déjà terminé, annulé ou rejeté.");
        }
        // Validate status transition
        if (!isValidTransition(rendezVous.getRendezVousStatut(), RendezVousStatut.Reschedule_Requested)) {
            throw new IllegalArgumentException("Invalid status transition from " + rendezVous.getRendezVousStatut() + " to Reschedule_Requested");
        }
        // Update status to Reschedule_Requested
        rendezVous.setRendezVousStatut(RendezVousStatut.Reschedule_Requested);
        rendezVous.setAppointmentDate(dto.getDate().toLocalDate());
        rendezVous.setAppointmentTime(dto.getDate().toLocalTime());
        rendezVous.setRendezVousType(dto.getRendeVousType().equalsIgnoreCase("EnPersonne") ? 
                RendezVousType.EnPersonne : RendezVousType.EnLigne);
        rendezVous.setReason(dto.getReason());
        rendezVous.setErrorMessage("Demande de reprogrammation envoyée par le patient.");
        rendezVousRepository.save(rendezVous);
        Long appointementPrice = rendezVous.getRendezVousType() == RendezVousType.EnPersonne ? 
                rendezVous.getMedecin().getPriceInPerson() : 
                rendezVous.getMedecin().getPriceOnline();
        return convertToDto(rendezVous, appointementPrice);

    }

    public RendeVousDTO cancelAppointment(Long patientId, Long appointmentId) {
        RendezVous rendezVous = rendezVousRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));
        if (!rendezVous.getPatient().getId().equals(patientId)) {
            throw new SecurityException("Non autorisé : vous n'êtes pas le patient de ce rendez-vous.");
        }
        if (rendezVous.getRendezVousStatut() == RendezVousStatut.Completed ||
            rendezVous.getRendezVousStatut() == RendezVousStatut.Cancelled ||
            rendezVous.getRendezVousStatut() == RendezVousStatut.Rejected) {
            throw new IllegalStateException("Impossible d'annuler un rendez-vous déjà terminé, annulé ou rejeté.");

        }
        // Validate status transition
        if (!isValidTransition(rendezVous.getRendezVousStatut(), RendezVousStatut.Cancelled)) {
            throw new IllegalArgumentException("Invalid status transition from " + rendezVous.getRendezVousStatut() + " to Cancelled");
        }
        if (rendezVous.getRendezVousStatut() == RendezVousStatut.Confirmed && rendezVous.getAppointmentDateTime().isBefore(LocalDateTime.now().plusHours(48))) {
            throw new IllegalStateException("Impossible d'annuler un rendez-vous confirmé moins de 48 heures avant la date.");
        }

        // Update status to Cancelled
        rendezVous.setRendezVousStatut(RendezVousStatut.Cancelled);
        rendezVous.setErrorMessage("Rendez-vous annulé par le patient.");
        rendezVousRepository.save(rendezVous);
        Long appointementPrice = rendezVous.getRendezVousType() == RendezVousType.EnPersonne ? 
                rendezVous.getMedecin().getPriceInPerson() : 
                rendezVous.getMedecin().getPriceOnline();
        return convertToDto(rendezVous, appointementPrice);
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

    public RendeVousDTO bookAppointment(Long patientId, RendezVousRequestDTO dto) {
        // Check if the medecin is available at the given date and time
        Medecin medecin = medecinService.getMedecinById(dto.getMedecinId());
        Patient patient = patientService.getPatientById(patientId);
        if (!medecin.getIsAvailable()) {
            throw new IllegalStateException("Le médecin n'est pas disponible.");
        }
        // Check if is there a rendezVous at the given date and time
        if (rendezVousRepository.findByAppointmentDateAndAppointmentTime(dto.getDate().toLocalDate(), dto.getDate().toLocalTime()).isPresent()) {
            throw new IllegalStateException("Le médecin n'est pas disponible à cette date et heure.");
        }

        // book the appointment
        RendezVous rendezVous = new RendezVous();
        rendezVous.setPatient(patient);
        rendezVous.setMedecin(medecin);
        rendezVous.setAppointmentDate(dto.getDate().toLocalDate());
        rendezVous.setAppointmentTime(dto.getDate().toLocalTime());
        rendezVous.setRendezVousStatut(RendezVousStatut.Pending);
        rendezVous.setRendezVousType(dto.getRendeVousType().equalsIgnoreCase("EnPersonne") ? RendezVousType.EnPersonne : RendezVousType.EnLigne);
        rendezVous.setReason(dto.getReason());
        rendezVousRepository.save(rendezVous);
        Long appointementPrice = rendezVous.getRendezVousType() == RendezVousType.EnPersonne ? medecin.getPriceInPerson() : medecin.getPriceOnline();
        return convertToDto(rendezVous, appointementPrice);
    }

    // get available date and time slots for a specific medecin in a given month only in the wordDay and without his Holidays

    public List<LocalDateTimeDTO> getAvailableDateTimeslots(Long medecinId, String month) {
        Medecin medecin = medecinService.getMedecinById(medecinId);
        List<LocalDateTimeDTO> availableDateTimeslots = new ArrayList<>();
    
        if (medecin.getIsAvailable()) {
            List<LocalDate> freeDays = getFreeDays(medecinId, month);
    
            for (LocalDate freeDay : freeDays) {
                List<LocalTime> availableTimes = getAvailableTimeSlots(medecinId, freeDay);
                for (LocalTime time : availableTimes) {
                    availableDateTimeslots.add(new LocalDateTimeDTO(freeDay, time, LocalDateTime.of(freeDay, time)));
                }
            }
        }
    
        return availableDateTimeslots;
    }
    
    public List<LocalTime> getAvailableTimeSlots(Long medecinId, LocalDate date) {
        Medecin medecin = medecinService.getMedecinById(medecinId);
    
        if (!medecin.getIsAvailable()) {
            throw new IllegalStateException("Le médecin n'est pas disponible.");
        }
    
        if (medecin.getStartTime() == null || medecin.getEndTime() == null) {
            throw new IllegalStateException("Les heures de travail du médecin ne sont pas définies.");
        }
    
        List<RendezVous> existingAppointments = rendezVousRepository
            .findByMedecinIdAndAppointmentDateAndRendezVousStatutIn(
                medecinId,
                date,
                List.of(RendezVousStatut.Confirmed, RendezVousStatut.Rescheduled)
            );
    
        Set<LocalTime> bookedSlots = existingAppointments.stream()
            .map(RendezVous::getAppointmentTime)
            .collect(Collectors.toSet());
    
        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime time = medecin.getStartTime();
    
        while (time.plusMinutes(30).minusNanos(1).isBefore(medecin.getEndTime()) || time.equals(medecin.getEndTime())) {
            if (LocalDate.now().equals(date) && time.isBefore(LocalTime.now())) {
                time = time.plusMinutes(30);
                continue;
            }
    
            if (!bookedSlots.contains(time)) {
                availableSlots.add(time);
            }
    
            time = time.plusMinutes(30);
        }
    
        return availableSlots;
    }

    public List<LocalDate> getFreeDays(Long medecinId, String month) {
    Medecin medecin = medecinService.getMedecinById(medecinId);

    if (!medecin.getIsAvailable()) {
        throw new IllegalStateException("Le médecin n'est pas disponible.");
    }

    // Parse the input month string (e.g., "2025-06")
    YearMonth yearMonth = YearMonth.parse(month); // format: "yyyy-MM"
    LocalDate startOfMonth = yearMonth.atDay(1);
    LocalDate endOfMonth = yearMonth.atEndOfMonth();

    // Get holidays as LocalDate set
    Set<LocalDate> holidayDates = medecin.getHolidays()
        .stream()
        .map(MedecinHoliday::getDate)
        .collect(Collectors.toSet());

    // Loop through each day of the month
    List<LocalDate> freeDays = new ArrayList<>();
    for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Check if the day is a work day and not a holiday
        if (medecin.getWorkingDays().contains(dayOfWeek) && !holidayDates.contains(date) && date.isAfter(LocalDate.now()) ) {
            freeDays.add(date);
        }
    }

    return freeDays;
}

}
