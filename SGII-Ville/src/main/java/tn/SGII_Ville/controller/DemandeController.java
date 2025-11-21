package tn.SGII_Ville.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.entities.Photo;
import tn.SGII_Ville.model.enums.EtatDemandeType;
import tn.SGII_Ville.service.DemandeXmlService;
import tn.SGII_Ville.service.FileStorageService;
import tn.SGII_Ville.service.InterventionXmlService;

/**
 * Contrôleur REST pour gérer les demandes citoyennes
 */
@RestController
@RequestMapping("/api/demandes")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandeController {

    private static final Logger logger = LoggerFactory.getLogger(DemandeController.class);

    @Autowired
    private DemandeXmlService demandeService;

    @Autowired
    private InterventionXmlService interventionService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${nominatim.contact:}")
    private String nominatimContact;

    // ==================== GET ALL DEMANDES ====================
    @GetMapping
    public ResponseEntity<List<Demande>> getAllDemandes() {
        try {
            List<Demande> demandes = demandeService.getAllDemandes();
            return ResponseEntity.ok(demandes);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de toutes les demandes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== GET DEMANDE BY ID ====================
    @GetMapping("/{id}")
    public ResponseEntity<Demande> getDemandeById(@PathVariable int id) {
        try {
            Demande demande = demandeService.findById(id);
            return (demande != null) ? ResponseEntity.ok(demande) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la demande ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CREATE DEMANDE (JSON ONLY) ====================
    @PostMapping
    public ResponseEntity<?> createDemande(@RequestBody Demande demande) {
        try {
            if (demande.getDescription() == null || demande.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La description est obligatoire"));
            }
            if (demande.getEtat() == null) demande.setEtat(EtatDemandeType.SOUMISE);
            if (demande.getDateSoumission() == null) demande.setDateSoumission(java.time.LocalDate.now());

            Demande nouvelle = demandeService.save(demande);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelle);
        } catch (Exception e) {
            logger.error("Erreur création demande JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur", "details", e.getMessage()));
        }
    }

    // ==================== REVERSE GEOCODE PROXY ====================
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        try {
            String contactEmail = nominatimContact != null && !nominatimContact.isBlank()
                    ? nominatimContact : "eya.boussarsar@example.com";

            String encodedEmail = URLEncoder.encode(contactEmail, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s&zoom=18&addressdetails=1&email=%s",
                    lat, lon, encodedEmail);

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "SGIIVILLE/1.0 (contact: " + contactEmail + ")")
                    .header("Accept", "application/json")
                    .header("Referer", "http://localhost:4200")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status == 200) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
            }
            if (status == 429) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", "Trop de requêtes Nominatim, réessayez plus tard"));
            }
            if (status == 403) {
                return ResponseEntity.ok(Map.of(
                        "warning", "Nominatim a bloqué la requête (403)",
                        "address", String.format("%s,%s", lat, lon),
                        "source", "coords"
                ));
            }

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Échec géocodage inverse", "status", status));

        } catch (Exception e) {
            logger.error("Erreur reverse-geocode lat={} lon={}", lat, lon, e);
            return ResponseEntity.ok(Map.of(
                    "address", String.format("%s,%s", lat, lon),
                    "source", "coords",
                    "warning", "Erreur géocodage"
            ));
        }
    }

    // ==================== CREATE DEMANDE WITH FILES (MULTIPART) ====================
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createDemandeMultipart(
            @RequestPart(value = "demande", required = false) String demandeJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        try {
            if (demandeJson == null || demandeJson.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le JSON 'demande' est requis"));
            }

            ObjectMapper om = new ObjectMapper();
            Demande demande = om.readValue(demandeJson, Demande.class);

            if (demande.getDescription() == null || demande.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La description est obligatoire"));
            }
            if (demande.getEtat() == null) demande.setEtat(EtatDemandeType.SOUMISE);

            // Gestion des photos
            if (files != null && files.length > 0) {
                List<Photo> savedPhotos = fileStorageService.storeFiles(files);
                List<Integer> photoIds = savedPhotos.stream().map(Photo::getIdPhoto).toList();
                demande.setPhotoRefs(photoIds);
            }

            Demande nouvelle = demandeService.save(demande);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelle);

        } catch (Exception e) {
            logger.error("Erreur création demande multipart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur serveur lors de la création", "details", e.getMessage()));
        }
    }

    // ==================== SERVE UPLOADED FILES ====================
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<byte[]> serveUpload(@PathVariable String filename) {
        try {
            Path p = fileStorageService.getFilePath(filename);
            if (p == null || !Files.exists(p)) return ResponseEntity.notFound().build();

            String contentType = Files.probeContentType(p);
            if (contentType == null) contentType = "application/octet-stream";

            byte[] data = Files.readAllBytes(p);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du fichier: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== PLANIFIER UNE DEMANDE (CORRIGÉ & FINAL) ====================
    
    @PostMapping("/planifier/{id}")
public ResponseEntity<?> planifierDemande(@PathVariable int id) {
    try {
        logger.info("DÉBUT PLANIFICATION DE LA DEMANDE #{}", id);

        Demande demande = demandeService.findById(id);
        if (demande == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Demande non trouvée : " + id));
        }

        if (demande.getEtat() == EtatDemandeType.TRAITEE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Cette demande est déjà planifiée."));
        }

        Intervention intervention = interventionService.planifierDemande(id);

        logger.info("PLANIFICATION RÉUSSIE → Intervention #{} créée", intervention.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(intervention);

    } catch (Exception e) {
        logger.error("ERREUR PLANIFICATION DEMANDE #{}", id, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la planification", "details", e.getMessage()));
    }
}
}