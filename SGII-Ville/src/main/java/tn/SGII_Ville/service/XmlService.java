package tn.SGII_Ville.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XmlService {

    private static final Logger logger = LoggerFactory.getLogger(XmlService.class);
    
    private static final String DATA_DIR = "src/main/resources/data/";
    private static final String INTERVENTIONS_DIR = DATA_DIR + "interventions/";
    public static final String NAMESPACE_URI = "http://example.com/gestion-interventions";

    private static final Map<String, String> SECTION_FILES = new HashMap<>();

    static {
        SECTION_FILES.put("Utilisateurs", "utilisateurs.xml");
        SECTION_FILES.put("Demandes", "demandes.xml");
        SECTION_FILES.put("Interventions", "interventions.xml");
        SECTION_FILES.put("Fournisseurs", "fournisseurs.xml");
        SECTION_FILES.put("Equipements", "equipements.xml");
        SECTION_FILES.put("RessourcesMaterielles", "ressources.xml");
        SECTION_FILES.put("MainDOeuvre", "maindoeuvre.xml");
        SECTION_FILES.put("Notifications", "notifications.xml");
        SECTION_FILES.put("DemandesAjout", "demandesajout.xml");
SECTION_FILES.put("Techniciens", "techniciens.xml");    }

    // ====================== MÉTHODES CLASSIQUES ======================
    public Document loadXmlDocument(String sectionName) throws Exception {
        String fileName = SECTION_FILES.get(sectionName);
        if (fileName == null) throw new IllegalArgumentException("Section inconnue: " + sectionName);
        File xmlFile = new File(DATA_DIR + fileName);
        
        if (!xmlFile.exists()) {
            logger.info("Fichier {} n'existe pas, création d'un nouveau", xmlFile.getPath());
            Document doc = createNewDocument(sectionName);
            saveXmlDocument(doc, sectionName);
            return doc;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }

    @Deprecated
    public Document loadXmlDocument() throws Exception {
        return loadXmlDocument("Utilisateurs");
    }

    public void saveXmlDocument(Document doc, String sectionName) throws Exception {
        String xsdResource = switch (sectionName) {
            case "Interventions" -> "/schemas/entities/interventions.xsd";
            case "Demandes"       -> "/schemas/entities/demandes.xsd";
            default               -> null;
        };

        if (xsdResource != null) {
            try {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(getClass().getResource(xsdResource));
                if (schema != null) {
                    Validator validator = schema.newValidator();
                    validator.validate(new DOMSource(doc));
                }
            } catch (Exception e) {
                logger.warn("Validation XSD échouée pour {}: {}", sectionName, e.getMessage());
            }
        }

        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        String fileName = SECTION_FILES.get(sectionName);
        if (fileName == null) throw new IllegalArgumentException("Section inconnue: " + sectionName);

        Path path = Paths.get(DATA_DIR + fileName);
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            tf.transform(new DOMSource(doc), new StreamResult(fos));
        }
        
        logger.info("Document sauvegardé: {}", path);
    }

    @Deprecated
    public void saveXmlDocument(Document doc) throws Exception {
        Element root = doc.getDocumentElement();
        String sectionName = root.getLocalName();
        saveXmlDocument(doc, sectionName);
    }

    public Document createNewDocument(String rootElementName) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElementNS(NAMESPACE_URI, rootElementName);
        doc.appendChild(root);
        return doc;
    }

    public Element createElement(Document doc, String tagName) {
        return doc.createElementNS(NAMESPACE_URI, tagName);
    }

    public void addTextElement(Document doc, Element parent, String tagName, String textContent) {
        Element element = createElement(doc, tagName);
        if (textContent != null) element.setTextContent(textContent);
        parent.appendChild(element);
    }

    public String getElementTextContent(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        return list.getLength() > 0 ? list.item(0).getTextContent() : null;
    }

    public int generateNewId(Document doc, String sectionName) {
        Element root = doc.getDocumentElement();
        NodeList children = root.getChildNodes();
        int maxId = 0;
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                String idStr = getElementTextContent(el, "id");
                if (idStr != null) {
                    try {
                        maxId = Math.max(maxId, Integer.parseInt(idStr));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return maxId + 1;
    }

    public boolean deleteElementById(Document doc, String sectionName, int id) {
        Element root = doc.getDocumentElement();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                String elId = getElementTextContent(el, "id");
                if (elId != null && Integer.parseInt(elId) == id) {
                    root.removeChild(el);
                    return true;
                }
            }
        }
        return false;
    }

    public String getNamespaceUri() {
        return NAMESPACE_URI;
    }

    // ====================== GESTION DES IDs INTERVENTIONS ======================
    public int getNextInterventionId() {
        try {
            logger.info("Calcul du prochain ID intervention...");
            Document doc = loadXmlDocument("Interventions");
            NodeList interventions = doc.getElementsByTagNameNS(getNamespaceUri(), "Intervention");
            
            logger.info("Nombre d'interventions existantes: {}", interventions.getLength());
            
            int maxId = 0;
            for (int i = 0; i < interventions.getLength(); i++) {
                Element intervention = (Element) interventions.item(i);
                String idText = getElementTextContent(intervention, "id");
                logger.info("Intervention trouvée - ID texte: {}", idText);
                if (idText != null && !idText.trim().isEmpty()) {
                    int id = Integer.parseInt(idText.trim());
                    if (id > maxId) maxId = id;
                }
            }
            
            int nextId = maxId + 1;
            logger.info("Prochain ID intervention: {}", nextId);
            return nextId;
            
        } catch (Exception e) {
            logger.error("Erreur getNextInterventionId", e);
            return 1;
        }
    }

    // ====================== INTERVENTIONS SÉPARÉES ======================
    public void saveInterventionAsSeparateFile(tn.SGII_Ville.entities.Intervention intervention) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        Element root = doc.createElementNS(NAMESPACE_URI, "Intervention");
        doc.appendChild(root);

        addTextElement(doc, root, "id", String.valueOf(intervention.getId()));
        addTextElement(doc, root, "priorite", intervention.getPriorite().name());
        addTextElement(doc, root, "etat", intervention.getEtat().name());
        addTextElement(doc, root, "datePlanifiee", intervention.getDatePlanifiee().toString());
        addTextElement(doc, root, "budget", intervention.getBudget().toPlainString());
        if (intervention.getTechnicienId() > 0) {
            addTextElement(doc, root, "technicienId", String.valueOf(intervention.getTechnicienId()));
        }

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(getClass().getResource("/schemas/entities/interventions.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(doc));
        } catch (Exception e) {
            logger.warn("Validation XSD échouée pour intervention {}: {}", intervention.getId(), e.getMessage());
        }

        new File(INTERVENTIONS_DIR).mkdirs();
        String path = INTERVENTIONS_DIR + "intervention_" + intervention.getId() + ".xml";

        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        tf.transform(new DOMSource(doc), new StreamResult(new File(path)));
        
        logger.info("Intervention sauvegardée dans: {}", path);
    }

    public tn.SGII_Ville.entities.Intervention loadInterventionById(int id) throws Exception {
        File file = new File(INTERVENTIONS_DIR + "intervention_" + id + ".xml");
        if (!file.exists()) return null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(file);
        Element root = doc.getDocumentElement();

        tn.SGII_Ville.entities.Intervention i = new tn.SGII_Ville.entities.Intervention();
        i.setId(Integer.parseInt(getElementTextContent(root, "id")));
        i.setPriorite(tn.SGII_Ville.model.enums.PrioriteType.valueOf(getElementTextContent(root, "priorite")));
        i.setEtat(tn.SGII_Ville.model.enums.EtatInterventionType.valueOf(getElementTextContent(root, "etat")));
        i.setDatePlanifiee(java.time.LocalDate.parse(getElementTextContent(root, "datePlanifiee")));
        i.setBudget(new java.math.BigDecimal(getElementTextContent(root, "budget")));
        String techId = getElementTextContent(root, "technicienId");
        if (techId != null && !techId.isEmpty()) {
            i.setTechnicienId(Integer.parseInt(techId));
        }
        return i;
    }

    public List<tn.SGII_Ville.entities.Intervention> getAllInterventionsFromFiles() throws Exception {
        List<tn.SGII_Ville.entities.Intervention> list = new ArrayList<>();
        File dir = new File(INTERVENTIONS_DIR);
        if (!dir.exists()) return list;

        File[] files = dir.listFiles((d, name) -> name.startsWith("intervention_") && name.endsWith(".xml"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                int id = Integer.parseInt(name.substring(13, name.indexOf('.')));
                tn.SGII_Ville.entities.Intervention i = loadInterventionById(id);
                if (i != null) list.add(i);
            }
        }
        return list;
    }

    // ====================== MÉTHODE DE RÉINITIALISATION ======================
    public void resetInterventionsFile() throws Exception {
        String filePath = DATA_DIR + "interventions.xml";
        File file = new File(filePath);
        
        if (file.exists()) {
            file.delete();
            logger.info("Fichier interventions.xml supprimé");
        }
        
        // Créer un nouveau fichier vide
        Document doc = createNewDocument("Interventions");
        saveXmlDocument(doc, "Interventions");
        logger.info("Fichier interventions.xml réinitialisé");
    }
}