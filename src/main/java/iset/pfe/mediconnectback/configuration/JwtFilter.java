package iset.pfe.mediconnectback.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import iset.pfe.mediconnectback.repositories.TokenRepository;
import iset.pfe.mediconnectback.services.JwtService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenRepository tokenRepo;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Only allow unauthenticated access to specific auth endpoints
        if (path.equals("/auth/login") || 
            path.equals("/auth/signup") || 
            path.equals("/auth/forgot-password") ||
            path.equals("/auth/verify-otp") ||
            path.equals("/auth/resetPassword") ||
            path.startsWith("/uploads/") || 
            path.startsWith("/ws/") ||
            path.startsWith("/chat/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendJsonError(response, "Missing or invalid Authorization header");
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractUserName(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                boolean isTokenValid = tokenRepo.findByToken(jwt)
                        .map(token -> !token.isExpired() && !token.isRevoked())
                        .orElse(false);

                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    sendJsonError(response, "Token is expired, revoked, or invalid");
                    return;
                }
            }
        } catch (MalformedJwtException e) {
            sendJsonError(response, "Malformed JWT token");
            return;
        } catch (JwtException e) {
            sendJsonError(response, "Invalid JWT token or signature");
            return;
        } catch (Exception e) {
            sendJsonError(response, "Unexpected error while validating JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendJsonError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
