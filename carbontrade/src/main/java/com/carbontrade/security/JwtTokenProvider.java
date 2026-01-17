package com.carbontrade.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @org.springframework.beans.factory.annotation.Value("${app.jwt.secret:default-dev-secret}")
    private String jwtSecret;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.refresh-expiration-ms:86400000}")
    private long jwtRefreshExpirationMs;

    private SecretKey secretKey;

    @javax.annotation.PostConstruct
    public void init() {
        if ("default-dev-secret".equals(jwtSecret)) {
            System.out.println("No JWT_SECRET provided. Generating a random secure key for this session.");
            this.secretKey = Jwts.SIG.HS512.key().build();
        } else {
            this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey) // algorithm inferred
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token); // correct JWS parsing
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
