package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.service.InterventionXmlService;
import tn.SGII_Ville.service.UserXmlService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur dédié aux techniciens
 * Endpoints accessibles uniquement aux utilisateurs authentifiés avec rôle TECHNICIEN
 */
@RestController
@RequestMapping("/api/technicien")
@CrossOrigin(origins = "http://localhost:4200")
public class TechnicienController {

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private UserXmlService userXmlService;

    /**
     * GET /api/technicien/interventions
     * Récupère uniquement les interventions assignées au technicien connecté
     */
    @GetMapping("/interventions")
    public ResponseEntity<List<Intervention>> getMyInterventions() {
        try {
            String email = getCurrentUserEmail();
            Utilisateur user = userXmlService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!(user instanceof Technicien)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<Intervention> allInterventions = interventionService.getAllInterventions();
            List<Intervention> myInterventions = allInterventions.stream()
                    .filter(i -> i.getTechnicienId() == user.getId())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(myInterventions);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * PATCH /api/technicien/interventions/{id}/terminer
     * Permet au technicien de marquer une de ses interventions comme TERMINEE
     */
    @PatchMapping("/interventions/{id}/terminer")
    public ResponseEntity<Intervention> terminerIntervention(@PathVariable int id) {
        try {
            Intervention intervention = interventionService.findById(id);
            if (intervention == null) {
                return ResponseEntity.notFound().build();
            }

            String email = getCurrentUserEmail();
            Utilisateur user = userXmlService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérification que c'est bien SON intervention
            if (intervention.getTechnicienId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            boolean updated = interventionService.updateEtat(id, EtatInterventionType.TERMINEE);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            Intervention updatedIntervention = interventionService.findById(id);
            return ResponseEntity.ok(updatedIntervention);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Méthode utilitaire pour récupérer l'email du user connecté
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // par défaut, Spring Security met l'username (ici l'email)
    }
    @GetMapping("/techniciens")
public ResponseEntity<List<Technicien>> getAllTechniciens() {
    try {
        // ON UTILISE LA NOUVELLE MÉTHODE QUI LIT techniciens.xml
        List<Technicien> techniciens = userXmlService.findAllTechniciensFromXml();
        return ResponseEntity.ok(techniciens);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
}