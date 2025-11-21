package tn.SGII_Ville.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.dto.LoginRequest;
import tn.SGII_Ville.dto.LoginResponse;
import tn.SGII_Ville.dto.RegisterRequest;
import tn.SGII_Ville.service.AuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour l'authentification
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /api/auth/login
     * Authentifie un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Erreur d'authentification", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/register
     * Enregistre un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur d'enregistrement", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/forgot-password
     * Initie la réinitialisation du mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(createErrorResponse("Erreur", "L'email est obligatoire"));
            }

            authService.forgotPassword(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Un email de réinitialisation a été envoyé à votre adresse");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/reset-password
     * Réinitialise le mot de passe avec un token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            if (token == null || token.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(createErrorResponse("Erreur", "Le token est obligatoire"));
            }

            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity
                        .badRequest()
                        .body(createErrorResponse("Erreur", "Le mot de passe doit contenir au moins 6 caractères"));
            }

            authService.resetPassword(token, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Votre mot de passe a été réinitialisé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erreur", e.getMessage()));
        }
    }

    /**
     * GET /api/auth/validate
     * Valide un token JWT
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Erreur", "Token manquant"));
            }

            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Erreur", "Token invalide"));
        }
    }

    /**
     * GET /api/auth/me
     * Récupère les informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Erreur", "Token manquant"));
            }

            String token = authHeader.substring(7);
            var utilisateur = authService.getUserFromToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("id", utilisateur.getId());
            response.put("nom", utilisateur.getNom());
            response.put("email", utilisateur.getEmail());
            response.put("role", utilisateur.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Erreur", e.getMessage()));
        }
    }

    /**
     * Crée une réponse d'erreur standardisée
     */
    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}
