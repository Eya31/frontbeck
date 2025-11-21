package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Intervention;
import tn.SGII_Ville.model.enums.EtatDemandeType;
import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.model.enums.PrioriteType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InterventionXmlService {

    private static final Logger logger = LoggerFactory.getLogger(InterventionXmlService.class);

    @Autowired private XmlService xmlService;
    @Autowired private DemandeXmlService demandeXmlService;

    public List<Intervention> getAllInterventions() {
        List<Intervention> interventions = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                interventions.add(parseIntervention((Element) nodes.item(i)));
            }

        } catch (Exception e) {
            logger.error("Erreur lors du chargement des interventions", e);
        }
        return interventions;
    }

    public Intervention findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    return parseIntervention(el);
                }
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'intervention {}", id, e);
        }
        return null;
    }

    public Intervention planifierDemande(int demandeId) {
        try {
            logger.info("=== DÉBUT PLANIFICATION DEMANDE #{} ===", demandeId);

            Demande demande = demandeXmlService.findById(demandeId);
            if (demande == null) {
                throw new RuntimeException("Demande non trouvée ID: " + demandeId);
            }
            
            logger.info("Demande trouvée: {} - État: {}", demande.getId(), demande.getEtat());

            if (demande.getEtat() == EtatDemandeType.TRAITEE) {
                throw new RuntimeException("Demande déjà planifiée");
            }

            // Générer nouvel ID intervention
            int newId = xmlService.getNextInterventionId();
            logger.info("Nouvel ID intervention généré: {}", newId);

            // Créer l'intervention
            Intervention intervention = new Intervention();
            intervention.setId(newId);
            intervention.setPriorite(PrioriteType.PLANIFIEE);
            intervention.setEtat(EtatInterventionType.EN_ATTENTE);
            intervention.setDatePlanifiee(LocalDate.now().plusDays(7));
            intervention.setBudget(new BigDecimal("800.00"));
            intervention.setTechnicienId(0);
            intervention.setDemandeId(demandeId);

            logger.info("Intervention créée: {}", intervention);

            // Sauvegarde dans le fichier principal
            saveToMainFile(intervention);

            // Mise à jour de la demande
            boolean ok = demandeXmlService.updateEtat(demandeId, EtatDemandeType.TRAITEE);
            if (!ok) {
                throw new RuntimeException("Échec mise à jour état demande");
            }

            logger.info("=== SUCCÈS → Intervention #{} créée ===", newId);
            return intervention;

        } catch (Exception e) {
            logger.error("=== ERREUR PLANIFICATION demande {} ===", demandeId, e);
            throw new RuntimeException("Échec planification: " + e.getMessage(), e);
        }
    }

    private void saveToMainFile(Intervention intervention) {
        try {
            logger.info("Sauvegarde intervention ID: {}", intervention.getId());
            
            Document doc = xmlService.loadXmlDocument("Interventions");
            Element root = doc.getDocumentElement();
            
            logger.info("Racine du document: {}", root.getNodeName());

            // Créer un nouvel élément Intervention
            Element newIntervention = doc.createElementNS(xmlService.getNamespaceUri(), "Intervention");
            
            // Ajouter tous les éléments enfants
            xmlService.addTextElement(doc, newIntervention, "id", String.valueOf(intervention.getId()));
            xmlService.addTextElement(doc, newIntervention, "priorite", intervention.getPriorite().name());
            xmlService.addTextElement(doc, newIntervention, "etat", intervention.getEtat().name());
            xmlService.addTextElement(doc, newIntervention, "datePlanifiee", intervention.getDatePlanifiee().toString());
            xmlService.addTextElement(doc, newIntervention, "budget", intervention.getBudget().toString());
            xmlService.addTextElement(doc, newIntervention, "technicienId", String.valueOf(intervention.getTechnicienId()));
            
            if (intervention.getDemandeId() != null) {
                xmlService.addTextElement(doc, newIntervention, "demandeId", String.valueOf(intervention.getDemandeId()));
            }

            // Ajouter à la racine
            root.appendChild(newIntervention);
            logger.info("Nouvel élément Intervention créé et ajouté");

            // Sauvegarder le document
            xmlService.saveXmlDocument(doc, "Interventions");
            logger.info("Document interventions sauvegardé avec succès");

        } catch (Exception e) {
            logger.error("ERREUR sauvegarde intervention:", e);
            throw new RuntimeException("Erreur sauvegarde intervention: " + e.getMessage(), e);
        }
    }

    private Intervention parseIntervention(Element el) {
        try {
            Intervention i = new Intervention();

            i.setId(Integer.parseInt(xmlService.getElementTextContent(el, "id")));
            i.setPriorite(PrioriteType.valueOf(xmlService.getElementTextContent(el, "priorite")));
            i.setEtat(EtatInterventionType.valueOf(xmlService.getElementTextContent(el, "etat")));
            i.setDatePlanifiee(LocalDate.parse(xmlService.getElementTextContent(el, "datePlanifiee")));
            i.setBudget(new BigDecimal(xmlService.getElementTextContent(el, "budget")));

            String tech = xmlService.getElementTextContent(el, "technicienId");
            i.setTechnicienId(tech != null ? Integer.parseInt(tech) : 0);

            String demandeId = xmlService.getElementTextContent(el, "demandeId");
            i.setDemandeId(demandeId != null ? Integer.parseInt(demandeId) : null);

            return i;

        } catch (Exception e) {
            logger.error("Erreur parsing intervention", e);
            throw new RuntimeException("Erreur parsing intervention", e);
        }
    }

    public boolean updateEtat(int id, EtatInterventionType nouvelEtat) {
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    NodeList etatNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "etat");
                    if (etatNodes.getLength() > 0) {
                        etatNodes.item(0).setTextContent(nouvelEtat.name());
                    }
                    xmlService.saveXmlDocument(doc, "Interventions");
                    return true;
                }
            }

        } catch (Exception e) {
            logger.error("Erreur mise à jour état intervention {}", id, e);
        }
        return false;
    }

    public boolean affecterTechnicien(int interventionId, int technicienId) {
        try {
            Document doc = xmlService.loadXmlDocument("Interventions");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Intervention");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == interventionId) {
                    // Mettre à jour technicienId
                    NodeList techNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "technicienId");
                    if (techNodes.getLength() > 0) {
                        techNodes.item(0).setTextContent(String.valueOf(technicienId));
                    }
                    
                    // Si intervention en attente → la mettre en cours
                    String currentEtat = xmlService.getElementTextContent(el, "etat");
                    if ("EN_ATTENTE".equals(currentEtat)) {
                        NodeList etatNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "etat");
                        if (etatNodes.getLength() > 0) {
                            etatNodes.item(0).setTextContent(EtatInterventionType.EN_COURS.name());
                        }
                    }

                    xmlService.saveXmlDocument(doc, "Interventions");
                    return true;
                }
            }

        } catch (Exception e) {
            logger.error("Erreur affectation technicien {} à intervention {}", technicienId, interventionId, e);
        }
        return false;
    }
}