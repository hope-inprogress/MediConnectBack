package iset.pfe.mediconnectback.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class DossierMedicalDTO {
    
  private Long id;
    private LocalDateTime dateCreated;
    private String patientName;
    private String patientImage;
    private List<DocumentMedicalDto> fichiers;
}

