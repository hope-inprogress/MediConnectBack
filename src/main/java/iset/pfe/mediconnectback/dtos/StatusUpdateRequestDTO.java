package iset.pfe.mediconnectback.dtos;

import iset.pfe.mediconnectback.enums.RendezVousStatut;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequestDTO {

    @NotNull
    private RendezVousStatut newStatus;

    private String errorMessage;

    @NotNull
    private Long doctorId;

    // Getters and Setters
    public RendezVousStatut getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(RendezVousStatut newStatus) {
        this.newStatus = newStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
