package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import tn.SGII_Ville.common.PointGeo;
import tn.SGII_Ville.entities.Demande;
import tn.SGII_Ville.entities.Photo;
import tn.SGII_Ville.model.enums.EtatDemandeType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemandeXmlService {

    @Autowired
    private XmlService xmlService;

    @Autowired
    private PhotoXmlService photoXmlService;

    /** ----------------------------------------
     *  GET ALL DEMANDES
     * ---------------------------------------- */
    public List<Demande> getAllDemandes() {
        List<Demande> demandes = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Demandes");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Demande");
            for (int i = 0; i < nodes.getLength(); i++) {
                demandes.add(parseDemande((Element) nodes.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return demandes;
    }

    /** ----------------------------------------
     *  FIND BY ID
     * ---------------------------------------- */
    public Demande findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Demandes");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Demande");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    return parseDemande(el);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** ----------------------------------------
     *  PARSE DEMANDE – CORRIGÉE POUR GÉRER LES <etat> SANS NAMESPACE
     * ---------------------------------------- */
    private Demande parseDemande(Element demandeElement) {

        int id = Integer.parseInt(xmlService.getElementTextContent(demandeElement, "id"));
        String description = xmlService.getElementTextContent(demandeElement, "description");
        LocalDate dateSoumission = LocalDate.parse(xmlService.getElementTextContent(demandeElement, "dateSoumission"));

        // ==================== CORRECTION CRITIQUE DE L'ÉTAT ====================
        String etatStr = xmlService.getElementTextContent(demandeElement, "etat");

        // Fallback si l’élément <etat> n’a pas le namespace (cas très fréquent)
        if (etatStr == null || etatStr.trim().isEmpty()) {
            NodeList fallback = demandeElement.getElementsByTagName("etat");
            if (fallback.getLength() > 0) {
                etatStr = fallback.item(0).getTextContent().trim();
            }
        }

        EtatDemandeType etat = EtatDemandeType.EN_ATTENTE; // valeur par défaut sûre
        if (etatStr != null && !etatStr.isEmpty()) {
            try {
                etat = EtatDemandeType.valueOf(etatStr);
            } catch (IllegalArgumentException ex) {
                System.err.println("État inconnu dans le XML («" + etatStr + "») → forcé à EN_ATTENTE");
            }
        }
        // ======================================================================

        String category = xmlService.getElementTextContent(demandeElement, "category");
        String subCategory = xmlService.getElementTextContent(demandeElement, "subCategory");
        String priority = xmlService.getElementTextContent(demandeElement, "priority");
        String contactEmail = xmlService.getElementTextContent(demandeElement, "contactEmail");

        // Localisation
        PointGeo localisation = null;
        String address = null;

        Element locEl = (Element) demandeElement.getElementsByTagNameNS(xmlService.getNamespaceUri(), "localisation").item(0);

        if (locEl != null) {
            float lat = Float.parseFloat(xmlService.getElementTextContent(locEl, "latitude"));
            float lng = Float.parseFloat(xmlService.getElementTextContent(locEl, "longitude"));
            address = xmlService.getElementTextContent(locEl, "address");

            localisation = new PointGeo();
            localisation.setLatitude(lat);
            localisation.setLongitude(lng);
        }

        // Photos
        List<Integer> photoRefs = new ArrayList<>();
        List<Photo> photos = new ArrayList<>();

        Element attachmentsEl = (Element) demandeElement.getElementsByTagNameNS(xmlService.getNamespaceUri(), "attachments").item(0);

        if (attachmentsEl != null) {
            NodeList refNodes = attachmentsEl.getElementsByTagNameNS(xmlService.getNamespaceUri(), "photoRef");

            for (int i = 0; i < refNodes.getLength(); i++) {
                try {
                    int photoId = Integer.parseInt(
                            xmlService.getElementTextContent((Element) refNodes.item(i), "id_photo")
                    );

                    photoRefs.add(photoId);

                    Photo p = photoXmlService.findById(photoId);
                    if (p != null) photos.add(p);

                } catch (Exception ignored) {}
            }
        }

        // Construction finale
        Demande demande = new Demande(id, description, dateSoumission, etat, localisation);

        demande.setCategory(category);
        demande.setSubCategory(subCategory);
        demande.setPriority(priority);
        demande.setContactEmail(contactEmail);
        demande.setAddress(address);
        demande.setPhotoRefs(photoRefs);
        demande.setPhotos(photos);

        return demande;
    }

    /** ----------------------------------------
     *  SAVE (inchangée)
     * ---------------------------------------- */
    public Demande save(Demande demande) {
        try {
            Document doc = xmlService.loadXmlDocument("Demandes");
            Element root = doc.getDocumentElement();

            int newId = generateNewId(doc);
            demande.setId(newId);

            Element demandeEl = doc.createElementNS(xmlService.getNamespaceUri(), "Demande");

            appendText(doc, demandeEl, "id", String.valueOf(newId));
            appendText(doc, demandeEl, "description", demande.getDescription());

            LocalDate date = demande.getDateSoumission() != null ? demande.getDateSoumission() : LocalDate.now();
            appendText(doc, demandeEl, "dateSoumission", date.toString());

            appendText(doc, demandeEl, "etat", demande.getEtat().name());
            appendText(doc, demandeEl, "category", demande.getCategory());
            appendText(doc, demandeEl, "subCategory", demande.getSubCategory());
            appendText(doc, demandeEl, "priority", demande.getPriority());
            appendText(doc, demandeEl, "contactEmail", demande.getContactEmail());

            // Localisation
            if (demande.getLocalisation() != null) {
                Element loc = doc.createElementNS(xmlService.getNamespaceUri(), "localisation");
                appendText(doc, loc, "latitude", String.valueOf(demande.getLocalisation().getLatitude()));
                appendText(doc, loc, "longitude", String.valueOf(demande.getLocalisation().getLongitude()));
                appendText(doc, loc, "address", demande.getAddress());
                demandeEl.appendChild(loc);
            }

            // Attachments
            if (demande.getPhotoRefs() != null && !demande.getPhotoRefs().isEmpty()) {
                Element attachments = doc.createElementNS(xmlService.getNamespaceUri(), "attachments");
                for (Integer ref : demande.getPhotoRefs()) {
                    Element refEl = doc.createElementNS(xmlService.getNamespaceUri(), "photoRef");
                    appendText(doc, refEl, "id_photo", String.valueOf(ref));
                    attachments.appendChild(refEl);
                }
                demandeEl.appendChild(attachments);
            }

            root.appendChild(demandeEl);
            xmlService.saveXmlDocument(doc, "Demandes");

            return demande;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur XML lors du save()", e);
        }
    }

    private void appendText(Document doc, Element parent, String tag, String text) {
        Element el = doc.createElementNS(xmlService.getNamespaceUri(), tag);
        if (text != null) el.setTextContent(text);
        parent.appendChild(el);
    }

    /** ----------------------------------------
     *  UPDATE ETAT – déjà ultra-robuste (on garde)
     * ---------------------------------------- */
    public boolean updateEtat(int id, EtatDemandeType nouvelEtat) {
        try {
            Document doc = xmlService.loadXmlDocument("Demandes");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Demande");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String idStr = xmlService.getElementTextContent(el, "id");
                if (idStr != null && Integer.parseInt(idStr) == id) {

                    NodeList etatNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "etat");
                    Node etatNode = null;

                    if (etatNodes.getLength() > 0) {
                        etatNode = etatNodes.item(0);
                    } else {
                        NodeList fallback = el.getElementsByTagName("etat");
                        if (fallback.getLength() > 0) {
                            etatNode = fallback.item(0);
                        }
                    }

                    if (etatNode == null) {
                        etatNode = doc.createElementNS(xmlService.getNamespaceUri(), "etat");
                        el.appendChild(etatNode);
                    }

                    etatNode.setTextContent(nouvelEtat.name());
                    xmlService.saveXmlDocument(doc, "Demandes");
                    System.out.println("État demande #" + id + " → " + nouvelEtat);
                    return true;
                }
            }
            System.err.println("Demande non trouvée pour ID: " + id);
            return false;

        } catch (Exception e) {
            System.err.println("ERREUR updateEtat demande " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** ----------------------------------------
     *  ADD PHOTOS TO DEMANDE (inchangée)
     * ---------------------------------------- */
    public List<Photo> addPhotosToDemande(int demandeId, List<Photo> photosToAdd) {
        List<Photo> added = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Demandes");

            Element target = null;
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Demande");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == demandeId) {
                    target = el;
                    break;
                }
            }

            if (target == null) throw new RuntimeException("Demande introuvable");

            Element attachments;
            NodeList attNodes = target.getElementsByTagNameNS(xmlService.getNamespaceUri(), "attachments");

            if (attNodes.getLength() > 0)
                attachments = (Element) attNodes.item(0);
            else {
                attachments = doc.createElementNS(xmlService.getNamespaceUri(), "attachments");
                target.appendChild(attachments);
            }

            int nextId = generateNextPhotoId(doc);

            for (Photo p : photosToAdd) {
                Element refEl = doc.createElementNS(xmlService.getNamespaceUri(), "photoRef");
                appendText(doc, refEl, "id_photo", String.valueOf(nextId));

                attachments.appendChild(refEl);
                added.add(new Photo(nextId, p.getUrl(), p.getNom()));

                nextId++;
            }

            xmlService.saveXmlDocument(doc, "Demandes");
            return added;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout de photos");
        }
    }

    /** ----------------------------------------
     *  Generate ID
     * ---------------------------------------- */
    private int generateNewId(Document doc) {
        NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Demande");
        int max = 0;

        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                int id = Integer.parseInt(xmlService.getElementTextContent((Element) nodes.item(i), "id"));
                if (id > max) max = id;
            } catch (Exception ignored) {}
        }

        return max + 1;
    }

    /** ----------------------------------------
     *  Generate next Photo ID
     * ---------------------------------------- */
    private int generateNextPhotoId(Document doc) {
        NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "id_photo");
        int max = 0;

        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                int id = Integer.parseInt(nodes.item(i).getTextContent());
                if (id > max) max = id;
            } catch (Exception ignored) {}
        }

        return max + 1;
    }
}