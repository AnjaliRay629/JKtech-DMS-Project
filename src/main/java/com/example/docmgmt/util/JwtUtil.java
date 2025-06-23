package com.example.docmgmt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.token.prefix}")
    private String tokenPrefix;

    @Value("${jwt.header}")
    private String header;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        if (token != null && token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length()).trim();
        }
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        if (token != null && token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length()).trim();
        }
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        try {
            String token = createToken(claims, userDetails.getUsername());
            return tokenPrefix + " " + token;
        } catch (Exception e) {
            logger.error("Error generating JWT token for user {}: {}", userDetails.getUsername(), e.getMessage());
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        if (secret == null || secret.length() < 64) {
            logger.error("JWT secret key is invalid: length={} (must be at least 64 characters for HS512)", secret != null ? secret.length() : 0);
            throw new IllegalStateException("JWT secret key must be at least 64 characters for HS512");
        }
        if (expiration == null || expiration <= 0) {
            logger.error("JWT expiration time is invalid: {}", expiration);
            throw new IllegalStateException("JWT expiration time is invalid");
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        if (token != null && token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length()).trim();
        }
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getHeader() {
        return header;
    }
}
