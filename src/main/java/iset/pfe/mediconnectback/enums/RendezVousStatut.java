package iset.pfe.mediconnectback.enums;

public enum RendezVousStatut {

    Pending, // Appointment requested by the patient
    Confirmed, // Appointment confirmed by the doctor
    Cancelled, // Appointment cancelled by the patient
    Rejected, // Appointment rejected by the doctor
    Completed, // Appointment completed
    No_Show // Patient did not show up for the appointment
    
}