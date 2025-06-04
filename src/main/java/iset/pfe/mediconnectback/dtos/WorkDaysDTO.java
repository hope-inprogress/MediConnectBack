package iset.pfe.mediconnectback.dtos;

import java.time.DayOfWeek;
import java.util.Set;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkDaysDTO {
    private Set<DayOfWeek> workDays;
}
