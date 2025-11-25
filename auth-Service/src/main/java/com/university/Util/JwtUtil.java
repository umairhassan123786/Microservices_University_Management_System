package com.university.Util;
import com.university.Entities.User;
import com.university.Repository.UserRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired
    private UserRepository userRepository;

    public String generateToken(String username, String role, Long userId, List<String> privileges) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId)
                .claim("privileges", privileges) // Add privileges to token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public List<String> getPrivilegesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        @SuppressWarnings("unchecked")
        List<String> privileges = (List<String>) claims.get("privileges");
        return privileges != null ? privileges : new ArrayList<>();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

//    public boolean validateToken(String token) {
//        try {
//            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
//            return true;
//        } catch (ExpiredJwtException e) {
//            System.out.println("Token expired");
//            return false;
//        } catch (Exception e) {
//            System.out.println("Token validation failed: " + e.getMessage());
//            return false;
//        }
//    }
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        if (claims.get("userId") != null) {
            return claims.get("userId", Long.class);
        } else {
            String username = claims.getSubject();
            Optional<User> user = userRepository.findByUsername(username);
            return user.map(User::getId).orElse(null);
        }
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");
            return false;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    // Naya method - Claims return karega
    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");
            throw new RuntimeException("Token expired");
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    // Existing method - Claims get karega without validation
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            System.out.println("Error getting claims from token: " + e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }
}