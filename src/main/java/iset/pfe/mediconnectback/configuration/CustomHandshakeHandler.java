package iset.pfe.mediconnectback.configuration;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserDetailsServiceImpl;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    
    private final JwtService jwtService;
    
    private final UserDetailsServiceImpl userDetailsService;

    public CustomHandshakeHandler(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
protected Principal determineUser(ServerHttpRequest request,
                                  WebSocketHandler wsHandler,
                                  Map<String, Object> attributes) {
    String token = (String) attributes.get("jwt");

    if (token != null) {
        String username = jwtService.extractUserName(token);
        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails)) {
                return new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
            }
        }
    }

    return null;
}

}

