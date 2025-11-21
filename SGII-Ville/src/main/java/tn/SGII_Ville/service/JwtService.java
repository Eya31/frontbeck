package tn.SGII_Ville.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.model.enums.RoleType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret:VotreCleSecreteTresLongueEtSecuriseeDeMinimum256BitsIciPourJWTTokenGeneration2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 heures par défaut
    private Long expiration;

    /**
     * Génère un token JWT pour un utilisateur
     */
    public String generateToken(Utilisateur utilisateur) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", utilisateur.getId());
        claims.put("email", utilisateur.getEmail());
        claims.put("nom", utilisateur.getNom());
        claims.put("role", utilisateur.getRole().name());

        return Jwts.builder()
                .claims(claims)
                .subject(utilisateur.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait l'email depuis le token
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extrait l'ID utilisateur depuis le token
     */
    public int extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }

    /**
     * Extrait le rôle depuis le token
     */
    public RoleType extractRole(String token) {
        String roleStr = extractClaims(token).get("role", String.class);
        return RoleType.valueOf(roleStr);
    }

    /**
     * Valide le token JWT
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Vérifie si le token est expiré
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * Extrait tous les claims du token
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Génère la clé de signature
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un token de réinitialisation de mot de passe (validité courte)
     */
    public String generatePasswordResetToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "PASSWORD_RESET");

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 heure
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valide un token de réinitialisation de mot de passe
     */
    public boolean validatePasswordResetToken(String token) {
        try {
            Claims claims = extractClaims(token);
            String type = claims.get("type", String.class);
            return "PASSWORD_RESET".equals(type) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
