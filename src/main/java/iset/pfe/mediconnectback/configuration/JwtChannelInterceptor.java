package iset.pfe.mediconnectback.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserDetailsServiceImpl;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // remove "Bearer "

                String username = jwtService.extractUserName(token); // ← use your method name

                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, userDetails)) {
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(auth); // set user for Principal in WebSocket
                        System.out.println("Token: " + token);
                        System.out.println("Username: " + username);
                        System.out.println("Is valid: " + jwtService.isTokenValid(token, userDetails));

                    }
                }
            }
        }

        return message;
    }
}
