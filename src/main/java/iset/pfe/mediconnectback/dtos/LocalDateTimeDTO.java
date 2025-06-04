package iset.pfe.mediconnectback.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalDateTimeDTO {
    private LocalDate date;
    private LocalTime time;
    private LocalDateTime dateTime;
}
