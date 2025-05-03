package iset.pfe.mediconnectback.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.OTP;
import iset.pfe.mediconnectback.entities.User;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long>{

	Optional<OTP> findByOtp(String otp);
	
	@Query("select t from OTP t where t.otp = ?1 and t.user =?2")
	public Optional<OTP> findByOtpAndUser (String otp, User user);
    public void deleteByUserEmailAndName(String email, String name);

	
}
