package tn.SGII_Ville.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration Spring Security avec JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF car on utilise JWT
            .csrf(csrf -> csrf.disable())
            
            // Configuration CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configuration des autorisations
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Endpoints ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRATEUR")
                .requestMatchers("/api/users/**").hasRole("ADMINISTRATEUR")
                
                // Endpoints DEMANDES
                .requestMatchers("/api/demandes/**").hasAnyRole("CITOYEN", "ADMINISTRATEUR", "CHEF_SERVICE", "TECHNICIEN")
                
                // Endpoints INTERVENTIONS
                .requestMatchers("/api/interventions/**").hasAnyRole("CHEF_SERVICE", "ADMINISTRATEUR", "TECHNICIEN")
                
                // Endpoints EQUIPEMENTS et RESSOURCES
                .requestMatchers("/api/equipements/**").hasAnyRole("CHEF_SERVICE", "ADMINISTRATEUR")
                .requestMatchers("/api/materiels/**").hasAnyRole("CHEF_SERVICE", "ADMINISTRATEUR")
                .requestMatchers("/api/demandes-ajout/**").hasAnyRole("CHEF_SERVICE", "ADMINISTRATEUR")
                
                // Endpoints TECHNICIEN
                .requestMatchers("/api/technicien/**").hasRole("TECHNICIEN")
                
                // Endpoints MAIN_OEUVRE
                .requestMatchers("/api/workforce/tasks").hasRole("MAIN_OEUVRE")
                
                // Toutes les autres requêtes nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Politique de session stateless (sans état)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Ajouter le filtre JWT avant le filtre d'authentification par défaut
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origines autorisées (à adapter selon votre frontend)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4200",
            "http://localhost:8080"
        ));
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));
        
        // Exposer les headers
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Autoriser les credentials
        configuration.setAllowCredentials(true);
        
        // Durée de cache de la configuration CORS
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Bean pour l'encodage des mots de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
