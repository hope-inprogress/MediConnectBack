package iset.pfe.mediconnectback.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
		User user = userRepo.findUserByEmail(userEmail)
            .orElseThrow(() -> {
                return new UsernameNotFoundException("User not found!");
            });
        String role = "ROLE_" + user.getRole().name().toUpperCase();
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );
	}

}
