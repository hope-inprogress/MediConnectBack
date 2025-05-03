package iset.pfe.mediconnectback.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
		
	@Value("${jwt.secret}")
	private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;
	
	public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
	}

    /*public String extractEmailFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return extractClaim(token, Claims::getSubject); // subject = email
    }*/
	
	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}


    private Claims extractAllClaims(String token) {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
      }
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
            final String username = extractUserName(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            return isValid;
        } catch (Exception e) {
            return false;
        }
	}
	
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

    public String generateToken(HashMap<String, Object> claims, UserDetails userDetails) {
        return buildToken(claims, userDetails, jwtExpiration);
    }

	private String buildToken(HashMap<String, Object> extraClaims, UserDetails userDetails, long expiration) {
		var authorities = userDetails.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.toList();
		return Jwts
				.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.claim("authorities", authorities)
				.signWith(getSignInKey(), SignatureAlgorithm.HS256)
				.compact();
	}

    public String generateRefreshToken(
        UserDetails userDetails
    ) {
      return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	
	//Extract Email from token;
	public String extractEmail(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
}
