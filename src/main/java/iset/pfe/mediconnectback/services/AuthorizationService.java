package iset.pfe.mediconnectback.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.User;
import iset.pfe.mediconnectback.repositories.UserRepository;

@Service
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

    public boolean isSelf(Long requestedId, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username = null;

        // Si le principal est une instance de User (dans le cas d'une authentification par email par exemple)
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else if (principal instanceof String) { 
            // Si le principal est un String (cela pourrait être l'email directement)
            username = (String) principal;
        }

        // Si username est nul, retourner false, car il n'y a pas d'utilisateur authentifié
        if (username == null) {
            return false;
        }

        // Recherche de l'utilisateur par email
        User user = userRepository.findUserByEmail(username).orElse(null);

        // Si l'utilisateur existe et que l'ID demandé correspond à l'ID de l'utilisateur, renvoie true
        return user != null && user.getId().equals(requestedId);
    }
}
