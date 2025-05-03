package iset.pfe.mediconnectback.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RequestMapping("/api")
@RestController
@CrossOrigin("http://localhost:5173")
public class TestController {
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDECIN', 'PATIENT')")
    @GetMapping("/test-controller")
    public ResponseEntity<?> sayHello() {
        return ResponseEntity.ok("Hello from secured endpoint");
    }
    
}
