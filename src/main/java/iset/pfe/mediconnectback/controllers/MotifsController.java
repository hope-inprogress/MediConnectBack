package iset.pfe.mediconnectback.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iset.pfe.mediconnectback.entities.Motifs;
import iset.pfe.mediconnectback.services.MotifsService;

@RestController
@RequestMapping("/motifs")
@CrossOrigin(origins = "http://localhost:5173")
public class MotifsController {
    
    @Autowired
    private MotifsService motifsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public List<Motifs> getMotifsByUserId(@PathVariable Long userId) {
        return motifsService.getMotifsDetailsByUserId(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/lastblock/{userId}")
    public LocalDate getLastBlockDate(@PathVariable Long userId) {
        return motifsService.getLastBlockDate(userId);
    }
}
