package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import tn.SGII_Ville.entities.Administrateur;
import tn.SGII_Ville.entities.ChefDeService;
import tn.SGII_Ville.entities.Citoyen;
import tn.SGII_Ville.entities.Technicien;
import tn.SGII_Ville.entities.Utilisateur;
import tn.SGII_Ville.model.enums.RoleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des utilisateurs dans le XML
 */
@Service
public class UserXmlService {

    @Autowired
    private XmlService xmlService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Trouve un utilisateur par email
     */
    public Optional<Utilisateur> findByEmail(String email) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element utilisateursSection = doc.getDocumentElement();
            
            if (utilisateursSection == null) return Optional.empty();

            NodeList children = utilisateursSection.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) child;
                    String userEmail = xmlService.getElementTextContent(userElement, "email");
                    
                    if (email.equals(userEmail)) {
                        return Optional.of(parseUtilisateur(userElement));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Trouve un utilisateur par ID
     */
    public Optional<Utilisateur> findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element utilisateursSection = doc.getDocumentElement();
            
            if (utilisateursSection == null) return Optional.empty();

            NodeList children = utilisateursSection.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) child;
                    String userId = xmlService.getElementTextContent(userElement, "id");
                    
                    if (userId != null && Integer.parseInt(userId) == id) {
                        return Optional.of(parseUtilisateur(userElement));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<Utilisateur> findAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element utilisateursSection = doc.getDocumentElement();
            
            if (utilisateursSection == null) return utilisateurs;

            NodeList children = utilisateursSection.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element userElement = (Element) child;
                    utilisateurs.add(parseUtilisateur(userElement));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }

    /**
     * Sauvegarde un nouvel utilisateur
     */
    public Utilisateur save(Utilisateur user) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            Element utilisateursSection = doc.getDocumentElement();
            
            if (utilisateursSection == null) {
                throw new RuntimeException("Section Utilisateurs introuvable");
            }

            // Générer un nouvel ID si nécessaire
            if (user.getId() == 0) {
                int newId = xmlService.generateNewId(doc, "Utilisateurs");
                user.setId(newId);
            }

            // Encoder le mot de passe si ce n'est pas déjà fait
            if (!user.getMotDePasse().startsWith("$2a$")) {
                user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
            }

            // Créer l'élément XML selon le type d'utilisateur
            Element userElement = createUserElement(doc, user);
            utilisateursSection.appendChild(userElement);

            // Sauvegarder le document
            xmlService.saveXmlDocument(doc, "Utilisateurs");

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde de l'utilisateur", e);
        }
    }

    /**
     * Met à jour un utilisateur existant
     */
    public Utilisateur update(Utilisateur utilisateur) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            
            // Supprimer l'ancien
            xmlService.deleteElementById(doc, "Utilisateurs", utilisateur.getId());
            
            // Recréer avec les nouvelles données
            Element utilisateursSection = doc.getDocumentElement();
            Element userElement = createUserElement(doc, utilisateur);
            utilisateursSection.appendChild(userElement);

            xmlService.saveXmlDocument(doc, "Utilisateurs");
            return utilisateur;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    /**
     * Supprime un utilisateur par ID
     */
    public boolean deleteById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Utilisateurs");
            boolean deleted = xmlService.deleteElementById(doc, "Utilisateurs", id);
            if (deleted) {
                xmlService.saveXmlDocument(doc, "Utilisateurs");
            }
            return deleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    /**
     * Parse un élément XML en objet Utilisateur
     */
    private Utilisateur parseUtilisateur(Element userElement) {
        String tagName = userElement.getLocalName();
        
        int id = Integer.parseInt(xmlService.getElementTextContent(userElement, "id"));
        String nom = xmlService.getElementTextContent(userElement, "nom");
        String email = xmlService.getElementTextContent(userElement, "email");
        String motDePasse = xmlService.getElementTextContent(userElement, "motDePasse");
        String roleStr = xmlService.getElementTextContent(userElement, "role");
        RoleType role = RoleType.valueOf(roleStr);

        return switch (tagName) {
            case "Citoyen" -> {
                String adresse = xmlService.getElementTextContent(userElement, "adresse");
                String telephone = xmlService.getElementTextContent(userElement, "telephone");
                yield new Citoyen(id, nom, email, motDePasse, adresse, telephone);
            }
            case "Technicien" -> {
                List<String> competences = parseCompetences(userElement);
                boolean disponibilite = Boolean.parseBoolean(
                    xmlService.getElementTextContent(userElement, "disponibilite")
                );
                yield new Technicien(id, nom, email, motDePasse, competences, disponibilite);
            }
            case "ChefDeService" -> {
                String departement = xmlService.getElementTextContent(userElement, "departement");
                yield new ChefDeService(id, nom, email, motDePasse, departement);
            }
            case "Administrateur" -> new Administrateur(id, nom, email, motDePasse);
            default -> throw new IllegalArgumentException("Type d'utilisateur inconnu: " + tagName);
        };
    }

    /**
     * Crée un élément XML à partir d'un objet Utilisateur
     */
    private Element createUserElement(Document doc, Utilisateur utilisateur) {
        Element userElement;
        
        if (utilisateur instanceof Citoyen citoyen) {
            userElement = xmlService.createElement(doc, "Citoyen");
            addBaseUserFields(doc, userElement, utilisateur);
            xmlService.addTextElement(doc, userElement, "adresse", citoyen.getAdresse());
            xmlService.addTextElement(doc, userElement, "telephone", citoyen.getTelephone());
        } 
        else if (utilisateur instanceof Technicien technicien) {
            userElement = xmlService.createElement(doc, "Technicien");
            addBaseUserFields(doc, userElement, utilisateur);
            
            for (String competence : technicien.getCompetences()) {
                xmlService.addTextElement(doc, userElement, "competences", competence);
            }
            
            xmlService.addTextElement(doc, userElement, "disponibilite", 
                String.valueOf(technicien.isDisponibilite()));
        } 
        else if (utilisateur instanceof ChefDeService chef) {
            userElement = xmlService.createElement(doc, "ChefDeService");
            addBaseUserFields(doc, userElement, utilisateur);
            xmlService.addTextElement(doc, userElement, "departement", chef.getDepartement());
        } 
        else if (utilisateur instanceof Administrateur) {
            userElement = xmlService.createElement(doc, "Administrateur");
            addBaseUserFields(doc, userElement, utilisateur);
        } 
        else {
            throw new IllegalArgumentException("Type d'utilisateur non supporté");
        }
        
        return userElement;
    }

    /**
     * Ajoute les champs de base d'un utilisateur
     */
    private void addBaseUserFields(Document doc, Element userElement, Utilisateur utilisateur) {
        xmlService.addTextElement(doc, userElement, "id", String.valueOf(utilisateur.getId()));
        xmlService.addTextElement(doc, userElement, "nom", utilisateur.getNom());
        xmlService.addTextElement(doc, userElement, "email", utilisateur.getEmail());
        xmlService.addTextElement(doc, userElement, "motDePasse", utilisateur.getMotDePasse());
        xmlService.addTextElement(doc, userElement, "role", utilisateur.getRole().name());
    }

    /**
     * Parse les compétences d'un technicien
     */
    private List<String> parseCompetences(Element userElement) {
        List<String> competences = new ArrayList<>();
        NodeList competenceNodes = userElement.getElementsByTagNameNS(
            xmlService.getNamespaceUri(), "competences"
        );
        
        for (int i = 0; i < competenceNodes.getLength(); i++) {
            competences.add(competenceNodes.item(i).getTextContent());
        }
        
        return competences;
    }

    /**
     * Récupère tous les techniciens
     */
    public List<Technicien> findAllTechniciens() {
        List<Technicien> techniciens = new ArrayList<>();
        try {
            List<Utilisateur> allUsers = findAll();
            for (Utilisateur user : allUsers) {
                if (user instanceof Technicien) {
                    techniciens.add((Technicien) user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return techniciens;
    }

    /**
     * Vérifie le mot de passe
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Encode un mot de passe
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    /**
 * Récupère TOUS les techniciens depuis techniciens.xml
 */
public List<Technicien> findAllTechniciensFromXml() {
    List<Technicien> techniciens = new ArrayList<>();
    try {
        Document doc = xmlService.loadXmlDocument("Techniciens"); // Grâce au mapping ci-dessus
        Element root = doc.getDocumentElement();

        NodeList nodes = root.getElementsByTagNameNS("http://example.com/gestion-interventions", "Technicien");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);

            int id = Integer.parseInt(xmlService.getElementTextContent(el, "id"));
            String nom = xmlService.getElementTextContent(el, "nom");
            String email = xmlService.getElementTextContent(el, "email");

            List<String> competences = new ArrayList<>();
            NodeList compNodes = el.getElementsByTagNameNS("http://example.com/gestion-interventions", "competences");
            for (int j = 0; j < compNodes.getLength(); j++) {
                competences.add(compNodes.item(j).getTextContent().trim());
            }

            boolean disponible = "true".equalsIgnoreCase(
                xmlService.getElementTextContent(el, "disponibilite")
            );

            Technicien tech = new Technicien(id, nom, email, null, competences, disponible);
            techniciens.add(tech);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return techniciens;
}
}
