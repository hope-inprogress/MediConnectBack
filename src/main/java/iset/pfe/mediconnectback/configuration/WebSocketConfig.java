package iset.pfe.mediconnectback.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import iset.pfe.mediconnectback.services.JwtService;
import iset.pfe.mediconnectback.services.UserDetailsServiceImpl;

import org.springframework.lang.NonNull;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtChannelInterceptor jwtChannelInterceptor;

        @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.setUserDestinationPrefix("/user");
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
            .setHandshakeHandler(new CustomHandshakeHandler(jwtService, userDetailsService))
            .setAllowedOrigins("http://localhost:5173")
            .addInterceptors(new JwtHandshakeInterceptor())
            .withSockJS();
    }

}
