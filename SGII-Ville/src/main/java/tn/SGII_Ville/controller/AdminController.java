package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.entities.ChefDeService;
import tn.SGII_Ville.service.UserXmlService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour les opérations admin
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private UserXmlService userXmlService;

    /**
     * GET /api/admin/users
     * Récupère tous les utilisateurs
     */
    @GetMapping("/users")
    public ResponseEntity<List<Utilisateur>> getAllUsers() {
        try {
            List<Utilisateur> users = userXmlService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/admin/techniciens
     * Récupère tous les techniciens
     */
    @GetMapping("/techniciens")
    public ResponseEntity<List<Technicien>> getAllTechniciens() {
        try {
            List<Technicien> techniciens = userXmlService.findAllTechniciens();
            return ResponseEntity.ok(techniciens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/admin/technicien
     * Crée un nouveau technicien
     */
    @PostMapping("/technicien")
    public ResponseEntity<?> createTechnicien(@RequestBody CreateTechnicienRequest request) {
        try {
            // Vérifier si l'email existe déjà
            if (userXmlService.findByEmail(request.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cet email est déjà utilisé");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            Technicien technicien = new Technicien();
            technicien.setNom(request.getNom());
            technicien.setEmail(request.getEmail());
            technicien.setMotDePasse(userXmlService.encodePassword(request.getMotDePasse()));
            technicien.setCompetences(request.getCompetences());
            technicien.setDisponibilite(true);

            Utilisateur savedUser = userXmlService.save(technicien);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du technicien");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /api/admin/chef-service
     * Crée un nouveau chef de service
     */
    @PostMapping("/chef-service")
    public ResponseEntity<?> createChefService(@RequestBody CreateChefServiceRequest request) {
        try {
            // Vérifier si l'email existe déjà
            if (userXmlService.findByEmail(request.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cet email est déjà utilisé");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            ChefDeService chef = new ChefDeService();
            chef.setNom(request.getNom());
            chef.setEmail(request.getEmail());
            chef.setMotDePasse(userXmlService.encodePassword(request.getMotDePasse()));
            chef.setDepartement(request.getDepartement());

            Utilisateur savedUser = userXmlService.save(chef);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du chef de service");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * DELETE /api/admin/users/{id}
     * Supprime un utilisateur
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        try {
            boolean deleted = userXmlService.deleteById(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Classe interne pour la création de technicien
     */
    public static class CreateTechnicienRequest {
        private String nom;
        private String email;
        private String motDePasse;
        private List<String> competences;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMotDePasse() { return motDePasse; }
        public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
        public List<String> getCompetences() { return competences; }
        public void setCompetences(List<String> competences) { this.competences = competences; }
    }

    /**
     * Classe interne pour la création de chef de service
     */
    public static class CreateChefServiceRequest {
        private String nom;
        private String email;
        private String motDePasse;
        private String departement;

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMotDePasse() { return motDePasse; }
        public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
        public String getDepartement() { return departement; }
        public void setDepartement(String departement) { this.departement = departement; }
    }
}
