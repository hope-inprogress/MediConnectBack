package iset.pfe.mediconnectback.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.enums.Sexe;
import iset.pfe.mediconnectback.enums.UserRole;
import iset.pfe.mediconnectback.enums.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	public Optional<User> findUserByEmail(String email);
    public List<User> findAllByRoleNot(UserRole role);
	Long countByUserStatus(UserStatus userStatus);
    Long countByRole(UserRole role);
    Long countByRoleAndSexe(UserRole role, Sexe sexe);
}
