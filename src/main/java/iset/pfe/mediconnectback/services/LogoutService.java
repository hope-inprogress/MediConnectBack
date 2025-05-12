package iset.pfe.mediconnectback.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import iset.pfe.mediconnectback.entities.Token;
import iset.pfe.mediconnectback.repositories.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LogoutService implements LogoutHandler{
    
    @Autowired 
    private TokenRepository tokenRepository;

    @Override
    public void logout(
        HttpServletRequest request,
        HttpServletResponse response, 
        Authentication authentication
        ) {
        final String authHeader = request.getHeader("Authorization");
        final String refreshHeader = request.getHeader("X-Refresh-Token");

        if (authHeader == null ||!authHeader.startsWith("Bearer ") || refreshHeader == null ) {
            return;
        }

        String access = authHeader.substring(7);
 
        Token accessToken = tokenRepository.findByToken(access)
            .orElse(null);

        if (accessToken != null) {
            accessToken.setExpired(true);
            accessToken.setRevoked(true);
            tokenRepository.save(accessToken);
            System.out.println("Access token revoked: " + accessToken.getToken());
        }

        Token refreshToken = tokenRepository.findByToken(refreshHeader)
        .orElse(null);

        if (refreshToken != null) {
            refreshToken.setExpired(true);
            refreshToken.setRevoked(true);
            tokenRepository.save(refreshToken);
            System.out.println("Refresh token revoked: " + refreshToken.getToken());
        }
        

        SecurityContextHolder.clearContext();
        System.out.println("Logout successful!");
        
    }
}
