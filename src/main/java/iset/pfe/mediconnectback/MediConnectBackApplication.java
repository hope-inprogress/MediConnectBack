package iset.pfe.mediconnectback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import iset.pfe.mediconnectback.entities.Admin;
import iset.pfe.mediconnectback.entities.DossierMedical;
import iset.pfe.mediconnectback.entities.Medecin;
import iset.pfe.mediconnectback.entities.OTP;
import iset.pfe.mediconnectback.entities.Patient;
import iset.pfe.mediconnectback.enums.AccountStatus;
import iset.pfe.mediconnectback.enums.Sexe;
import iset.pfe.mediconnectback.enums.Specialite;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.enums.UserStatus;
import iset.pfe.mediconnectback.repositories.DossierMedicalRepository;
import iset.pfe.mediconnectback.repositories.MedecinRepository;
import iset.pfe.mediconnectback.repositories.MotifsRepository;
import iset.pfe.mediconnectback.repositories.OTPRepository;
import iset.pfe.mediconnectback.repositories.PatientRepository;
import iset.pfe.mediconnectback.repositories.RendezVousRepository;
import iset.pfe.mediconnectback.repositories.UserRepository;
import iset.pfe.mediconnectback.services.MedecinService;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MediConnectBackApplication {

    private final MedecinRepository medecinRepository;

    MediConnectBackApplication(MedecinRepository medecinRepository) {
        this.medecinRepository = medecinRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(MediConnectBackApplication.class, args);
    }

    @Bean 
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    CommandLineRunner initData(
            OTPRepository otpRepository,
            MedecinRepository medecinRepository,
            PatientRepository patientRepository,
            MedecinService medecinService,
            UserRepository userRepository,
            RendezVousRepository rendezVousRepository,
            DossierMedicalRepository dossierMedicalRepository,
            MotifsRepository motifRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            List<OTP> existingOtps = otpRepository.findAll();
            for (OTP otp : existingOtps) {
                if (otp.getValidatedAt() != null && otp.getValidatedAt().isBefore(LocalDateTime.now().minusDays(2))) {
                    otpRepository.delete(otp);
                } else if (otp.getValidatedAt() == null && otp.getExpiresAt().isBefore(LocalDateTime.now().minusDays(2))) {
                    otpRepository.delete(otp);
                }
            }

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
                for (int i = 1; i <= 10; i++) {
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
                    patient.setUserStatus(UserStatus.Active);
                    patient.setAccountStatus(AccountStatus.Verified);
					userRepository.save(patient); 
                }

                // Create Medecins
                for (int i = 1 ; i <= 10; i++) {
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
                    medecin.setSpecialitePrimaire(Specialite.GENERAL_MEDICINE);
                    medecin.setSpecialiteSecondaire(new ArrayList<>(List.of(Specialite.PEDIATRICS)));
                    medecin.setWorkPlace("Hopital " + i);
                    medecin.setStartTime(LocalTime.of(8, 0));
                    medecin.setEndTime(LocalTime.of(16, 0));
                    medecin.setIsAvailable(i % 2 == 0);
                    medecin.setUserStatus(UserStatus.Active);
                    medecin.setAccountStatus(AccountStatus.Verified);
					medecinRepository.save(medecin);
                        
                    
				}
			}

            List<Patient> patients = patientRepository.findAll();
            for (Patient patient : patients) {
                if (patient.getDossierMedical() == null) {
                    DossierMedical dossierMedical = new DossierMedical();
                    dossierMedical.setDateCreated(LocalDateTime.now());
                    dossierMedical.setPatient(patient);
                    dossierMedical = dossierMedicalRepository.save(dossierMedical);
                    patient.setDossierMedical(dossierMedical);
                    patientRepository.save(patient);
                }
            }







		};
	}
}


