package iset.pfe.mediconnectback.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentMedicalDto {
    private Long id;
    private String fichier;
    private String type;
    private String visibility;
    private String uploaderName;
    private String uploaderImage;
    private LocalDateTime uploadDate;
}
