// src/main/java/tn/SGII_Ville/controller/InterventionController.java
package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.model.enums.EtatDemandeType;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.service.DemandeXmlService;
import tn.SGII_Ville.service.InterventionXmlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Contrôleur REST pour gérer les interventions et la planification des demandes
 */
@RestController
@RequestMapping("/api/interventions")
@CrossOrigin(origins = "http://localhost:4200")
public class InterventionController {

    private static final Logger logger = LoggerFactory.getLogger(InterventionController.class);

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private DemandeXmlService demandeService;

    // -------------------------------------------------------
    // 1. PLANIFIER UNE DEMANDE → crée automatiquement intervention
    // -------------------------------------------------------
    @PostMapping("/planifier/{id}")
public ResponseEntity<?> planifierDemande(@PathVariable int id) {
    try {
        logger.info("=== DÉBUT PLANIFICATION DEMANDE #{} ===", id);

        Demande demande = demandeService.findById(id);
        logger.info("Demande trouvée: {}", demande);

        if (demande == null) {
            logger.error("Demande non trouvée ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("État actuel de la demande: {}", demande.getEtat());

        if (demande.getEtat() == EtatDemandeType.TRAITEE) {
            logger.warn("Demande déjà planifiée ID: {}", id);
            return ResponseEntity.badRequest()
                    .body("Cette demande est déjà planifiée.");
        }

        // Création intervention
        logger.info("Appel à interventionService.planifierDemande...");
        Intervention intervention = interventionService.planifierDemande(id);
        logger.info("Intervention créée avec succès: {}", intervention);

        return ResponseEntity.status(HttpStatus.CREATED).body(intervention);

    } catch (Exception e) {
        logger.error("=== ERREUR CRITIQUE planification demande {} ===", id, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur : " + e.getMessage());
    }
}

    // -------------------------------------------------------
    // 2. Récupérer toutes les interventions
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Intervention>> getAllInterventions() {
        try {
            return ResponseEntity.ok(interventionService.getAllInterventions());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------
    // 3. Récupérer une intervention par ID
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Intervention> getInterventionById(@PathVariable int id) {
        try {
            Intervention intervention = interventionService.findById(id);
            return (intervention != null)
                    ? ResponseEntity.ok(intervention)
                    : ResponseEntity.notFound().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------
    // 4. Modifier le statut d’une intervention
    // -------------------------------------------------------
    @PatchMapping("/{id}")
    public ResponseEntity<Intervention> updateInterventionStatus(
            @PathVariable int id,
            @RequestBody StatutUpdate statutUpdate) {
        try {
            Intervention intervention = interventionService.findById(id);
            if (intervention == null) {
                return ResponseEntity.notFound().build();
            }

            boolean updated = interventionService.updateEtat(id, statutUpdate.getStatut());
            if (!updated) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------
    // 5. Affecter un technicien à une intervention
    // -------------------------------------------------------
    @PutMapping("/{id}/affecter")
    public ResponseEntity<Intervention> affecterTechnicien(
            @PathVariable int id,
            @RequestBody AffectationRequest request) {
        try {
            Intervention intervention = interventionService.findById(id);
            if (intervention == null) {
                return ResponseEntity.notFound().build();
            }

            boolean updated = interventionService.affecterTechnicien(id, request.getTechnicienId());
            if (!updated) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            return ResponseEntity.ok(interventionService.findById(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // -------------------------------------------------------
    // DTO internes
    // -------------------------------------------------------
    public static class StatutUpdate {
        private EtatInterventionType statut;

        public EtatInterventionType getStatut() {
            return statut;
        }
        public void setStatut(EtatInterventionType statut) {
            this.statut = statut;
        }
    }

    public static class AffectationRequest {
        private int technicienId;

        public int getTechnicienId() {
            return technicienId;
        }
        public void setTechnicienId(int technicienId) {
            this.technicienId = technicienId;
        }
    }
}
