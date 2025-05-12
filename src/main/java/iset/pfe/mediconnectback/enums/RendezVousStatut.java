package iset.pfe.mediconnectback.enums;

import java.time.LocalDateTime;

public enum RendezVousStatut {

    Pending, // Appointment requested by the patient
    Confirmed, // Appointment confirmed by the doctor
    Cancelled, // Appointment cancelled by the patient
    Rejected, // Appointment rejected by the doctor
    Completed, // Appointment completed
    No_Show, // Patient did not show up for the appointment
    Rescheduled ,// Appointment rescheduled
    Reschedule_Requested; // Appointment reschedule requested by the patient
    
    
    // Helper methods
    public boolean isTerminal() {
        return this == Rejected || 
               this == Cancelled || 
               this == Completed ||
               this == No_Show;
    }

    public boolean canPatientCancel() {
        return this == Confirmed || 
               this == Reschedule_Requested;
    }

    public boolean requiresDoctorApproval() {
        return this == Reschedule_Requested;
    }

    public boolean isAutoTransition() {
        return this == Rejected || this == Confirmed;
    }

    public static boolean isValidReschedule(LocalDateTime newTime, 
                                          LocalDateTime now,
                                          int rescheduleCount) {
        return !newTime.toLocalDate().equals(now.toLocalDate()) &&
               rescheduleCount == 0 &&
               newTime.isAfter(now.plusHours(48));
    }
}