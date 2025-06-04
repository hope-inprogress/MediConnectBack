package iset.pfe.mediconnectback.dtos;

import java.util.List;
import lombok.Data;

@Data
public class FavoriteMedecinListDTO {
    private Long id;
    private List<MedecinDTO> medecins;
} 