// tn.SGII_Ville.controller.MapController.java
package tn.SGII_Ville.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.service.DemandeXmlService;
import tn.SGII_Ville.service.InterventionXmlService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = "http://localhost:4200")
public class MapController {

    @Autowired
    private DemandeXmlService demandeService;

    @Autowired
    private InterventionXmlService interventionService;

    /**
     * Endpoint pour récupérer toutes les données de la carte
     * Retourne les demandes et interventions avec leurs localisations
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getMapData() {
        try {
            Map<String, Object> mapData = new HashMap<>();
            
            // Récupérer les demandes
            List<Demande> demandes = demandeService.getAllDemandes();
            List<Intervention> interventions = interventionService.getAllInterventions();
            
            mapData.put("demandes", demandes);
            mapData.put("interventions", interventions);
            
            return ResponseEntity.ok(mapData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint pour récupérer uniquement les points des demandes actives
     */
    @GetMapping("/demandes-points")
    public ResponseEntity<List<Demande>> getDemandePoints() {
        try {
            List<Demande> demandes = demandeService.getAllDemandes();
            // Filtrer les demandes avec localisation
            List<Demande> demandesAvecLocalisation = demandes.stream()
                .filter(d -> d.getLocalisation() != null && 
                           d.getLocalisation().getLatitude() != 0 && 
                           d.getLocalisation().getLongitude() != 0)
                .toList();
            
            return ResponseEntity.ok(demandesAvecLocalisation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint pour récupérer uniquement les points des interventions
     */
    @GetMapping("/interventions-points")
    public ResponseEntity<List<Intervention>> getInterventionPoints() {
        try {
            List<Intervention> interventions = interventionService.getAllInterventions();
            return ResponseEntity.ok(interventions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}