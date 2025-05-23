package iset.pfe.mediconnectback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import iset.pfe.mediconnectback.entities.Admin;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.enums.AccountStatus;
import iset.pfe.mediconnectback.enums.Sexe;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.repositories.MotifsRepository;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;
import iset.pfe.mediconnectback.repositories.UserRepository;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MediConnectBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediConnectBackApplication.class, args);
    }

    /*@Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            MedecinRepository medecinRepository,
            RendezVousRepository rendezVousRepository,
            MotifsRepository motifRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() == 0) {

                // Create Admin
                Admin admin = new Admin();
                admin.setFirstName("Admin");
                admin.setLastName("Root");
                admin.setEmail("admin@mediconnect.com");
                admin.setPassword(passwordEncoder.encode("123456789"));
                admin.setRole(UserRole.Admin);
                admin.setUserStatus(UserStatus.Active);
                admin.setAccountStatus(AccountStatus.Verified);
                admin.setSexe(Sexe.Femme);
                admin.setAddress("123 Admin Street");
                admin.setPhoneNumber(12345678);
                admin.setDateNaissance(LocalDate.of(1990, 1, 1));
				admin.setCreatedDate(LocalDateTime.now());
                userRepository.save(admin);

                // Create Patients
                for (int i = 1; i <= 5; i++) {
                    Patient patient = new Patient();
                    patient.setFirstName("Patient" + i);
                    patient.setLastName("Test");
                    patient.setEmail("patient" + i + "@mediconnect.com");
                    patient.setPassword(passwordEncoder.encode("123456789"));
                    patient.setRole(UserRole.Patient);
                    patient.setSexe(i % 2 == 0 ? Sexe.Homme : Sexe.Femme);
                    patient.setAddress("City " + i);
                    patient.setPhoneNumber(10000000 + i);
                    patient.setDateNaissance(LocalDate.of(1995, i, i));
					patient.setCreatedDate(LocalDateTime.now());

                    // Status handling
                    /*if (i % 3 == 0) {
                        patient.setUserStatus(UserStatus.Blocked);
                        patient.setAccountStatus(AccountStatus.NotVerified);

						userRepository.save(patient);

                        Motifs motif = new Motifs();
						motif.setUser(patient);
						motif.setEventType("BLOCK");
						motif.setEventTime(LocalDate.now());
						motif.setReason("Patient blocked for violating terms");
                        motifRepository.save(motif);
                    } else if (i % 3 == 1) {
                        patient.setUserStatus(UserStatus.Undecided);
                        patient.setAccountStatus(AccountStatus.Verified);
						userRepository.save(patient);
                    } else {
                        
                    }
                    
                        patient.setUserStatus(UserStatus.Active);
                        patient.setAccountStatus(AccountStatus.Verified);
						userRepository.save(patient);
                    
                }

                // Create Medecins
                for (int i = 1; i <= 5; i++) {
                    Medecin medecin = new Medecin();
                    medecin.setFirstName("Medecin" + i);
                    medecin.setLastName("Test");
                    medecin.setEmail("medecin" + i + "@mediconnect.com");
                    medecin.setPassword(passwordEncoder.encode("123456789"));
                    medecin.setRole(UserRole.Medecin);
                    medecin.setSexe(i % 2 == 0 ? Sexe.Homme : Sexe.Femme);
                    medecin.setAddress("Clinic Street " + i);
                    medecin.setPhoneNumber(20000000 + i);
                    medecin.setDateNaissance(LocalDate.of(1980 + i, i, i));
					medecin.setCreatedDate(LocalDateTime.now());
                    medecin.setCodeMedical("CMED" + i);
                    medecin.setSpecialite("Specialite" + i);
                    medecin.setWorkPlace("Hopital " + i);
                    medecin.setStartTime(LocalTime.of(8, 0));
                    medecin.setEndTime(LocalTime.of(16, 0));
                    medecin.setIsAvailable(i % 2 == 0);
                    medecin.setUserStatus(UserStatus.Active);
                    medecin.setAccountStatus(AccountStatus.Verified);
					medecinRepository.save(medecin);
                        
                    
				}
			}
		};
	}*/
}

