package tn.SGII_Ville.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import tn.SGII_Ville.common.PointGeo;
import tn.SGII_Ville.entities.Equipement;
import tn.SGII_Ville.entities.Fournisseur;
import tn.SGII_Ville.model.enums.EtatEquipementType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class EquipementXmlService {

    @Autowired
    private XmlService xmlService;

    

    public List<Equipement> getAllEquipements() {
        List<Equipement> equipements = new ArrayList<>();
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Equipement");
            for (int i = 0; i < nodes.getLength(); i++) {
                equipements.add(parseEquipement((Element) nodes.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return equipements;
    }

    public Equipement findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Equipement");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    return parseEquipement(el);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Equipement parseEquipement(Element equipementElement) {
        int id = Integer.parseInt(xmlService.getElementTextContent(equipementElement, "id"));
        String type = xmlService.getElementTextContent(equipementElement, "type");
        EtatEquipementType etat = EtatEquipementType.valueOf(
            xmlService.getElementTextContent(equipementElement, "etat")
        );
        BigDecimal valeurAchat = new BigDecimal(xmlService.getElementTextContent(equipementElement, "valeurAchat"));

        // Parse Fournisseur
        Fournisseur fournisseur = null;
        Element fournisseurEl = (Element) equipementElement.getElementsByTagNameNS(
            xmlService.getNamespaceUri(), "fournisseur").item(0);
        if (fournisseurEl != null) {
            int fournisseurId = Integer.parseInt(xmlService.getElementTextContent(fournisseurEl, "id"));
            String nom = xmlService.getElementTextContent(fournisseurEl, "nom");
            String contact = xmlService.getElementTextContent(fournisseurEl, "contact");
            String telephone = xmlService.getElementTextContent(fournisseurEl, "telephone");
            // The Fournisseur entity uses 'email' instead of 'contact' and requires an 'adresse' argument.
            // Map the XML 'contact' element to the Fournisseur email and provide an empty adresse when missing.
            fournisseur = new Fournisseur(fournisseurId, nom, contact, telephone, "");
        }

        // Parse Localisation
        PointGeo localisation = null;
        Element locEl = (Element) equipementElement.getElementsByTagNameNS(
            xmlService.getNamespaceUri(), "localisation").item(0);
        if (locEl != null) {
            float lat = Float.parseFloat(xmlService.getElementTextContent(locEl, "latitude"));
            float lng = Float.parseFloat(xmlService.getElementTextContent(locEl, "longitude"));
            localisation = new PointGeo(lat, lng);
        }

        return new Equipement(id, type, etat, fournisseur, valeurAchat, localisation);
    }

    public Equipement save(Equipement equipement) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            Element root = doc.getDocumentElement();

            int newId = generateNewId(doc);
            equipement.setId(newId);

            Element equipementEl = doc.createElementNS(xmlService.getNamespaceUri(), "Equipement");

            xmlService.addTextElement(doc, equipementEl, "id", String.valueOf(newId));
            xmlService.addTextElement(doc, equipementEl, "type", equipement.getType());
            xmlService.addTextElement(doc, equipementEl, "etat", equipement.getEtat().name());
            xmlService.addTextElement(doc, equipementEl, "valeurAchat", 
                equipement.getValeurAchat() != null ? equipement.getValeurAchat().toString() : "0");

            // Fournisseur
            if (equipement.getFournisseur() != null) {
                Element fournisseurEl = doc.createElementNS(xmlService.getNamespaceUri(), "fournisseur");
                Fournisseur f = equipement.getFournisseur();
                xmlService.addTextElement(doc, fournisseurEl, "id", String.valueOf(f.getId()));
                xmlService.addTextElement(doc, fournisseurEl, "nom", f.getNom());
                // Use email accessor since Fournisseur stores contact as 'email'
                xmlService.addTextElement(doc, fournisseurEl, "contact", f.getEmail());
                xmlService.addTextElement(doc, fournisseurEl, "telephone", f.getTelephone());
                equipementEl.appendChild(fournisseurEl);
            }

            // Localisation
            if (equipement.getLocalisation() != null) {
                Element locEl = doc.createElementNS(xmlService.getNamespaceUri(), "localisation");
                xmlService.addTextElement(doc, locEl, "latitude", 
                    String.valueOf(equipement.getLocalisation().getLatitude()));
                xmlService.addTextElement(doc, locEl, "longitude", 
                    String.valueOf(equipement.getLocalisation().getLongitude()));
                equipementEl.appendChild(locEl);
            }

            root.appendChild(equipementEl);
            xmlService.saveXmlDocument(doc, "Equipements");

            return equipement;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur création équipement", e);
        }
    }

    public Equipement update(int id, Equipement equipement) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            NodeList nodes = doc.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Equipement");
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (Integer.parseInt(xmlService.getElementTextContent(el, "id")) == id) {
                    
                    // Mettre à jour les champs
                    updateElementTextContent(el, "type", equipement.getType());
                    updateElementTextContent(el, "etat", equipement.getEtat().name());
                    updateElementTextContent(el, "valeurAchat", 
                        equipement.getValeurAchat() != null ? equipement.getValeurAchat().toString() : "0");

                    // Fournisseur: update or create
                    if (equipement.getFournisseur() != null) {
                        NodeList fournNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "fournisseur");
                        Element fournEl;
                        if (fournNodes.getLength() > 0) {
                            fournEl = (Element) fournNodes.item(0);
                        } else {
                            fournEl = doc.createElementNS(xmlService.getNamespaceUri(), "fournisseur");
                            el.appendChild(fournEl);
                        }
                        Fournisseur f = equipement.getFournisseur();
                        updateOrCreateTextElement(doc, fournEl, "id", String.valueOf(f.getId()));
                        updateOrCreateTextElement(doc, fournEl, "nom", f.getNom());
                        // map email -> contact in XML
                        updateOrCreateTextElement(doc, fournEl, "contact", f.getEmail());
                        updateOrCreateTextElement(doc, fournEl, "telephone", f.getTelephone());
                    }

                    // Localisation: update or create
                    if (equipement.getLocalisation() != null) {
                        NodeList locNodes = el.getElementsByTagNameNS(xmlService.getNamespaceUri(), "localisation");
                        Element locEl;
                        if (locNodes.getLength() > 0) {
                            locEl = (Element) locNodes.item(0);
                        } else {
                            locEl = doc.createElementNS(xmlService.getNamespaceUri(), "localisation");
                            el.appendChild(locEl);
                        }
                        updateOrCreateTextElement(doc, locEl, "latitude", String.valueOf(equipement.getLocalisation().getLatitude()));
                        updateOrCreateTextElement(doc, locEl, "longitude", String.valueOf(equipement.getLocalisation().getLongitude()));
                    }

                    xmlService.saveXmlDocument(doc, "Equipements");
                    return equipement;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Equipements");
            boolean deleted = xmlService.deleteElementById(doc, "Equipements", id);
            if (deleted) {
                xmlService.saveXmlDocument(doc, "Equipements");
            }
            return deleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateElementTextContent(Element parent, String tagName, String textContent) {
        NodeList nodes = parent.getElementsByTagNameNS(xmlService.getNamespaceUri(), tagName);
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(textContent);
        }
    }

    private int generateNewId(Document doc) {
        return xmlService.generateNewId(doc, "Equipements");
    }

    private void updateOrCreateTextElement(Document doc, Element parent, String tagName, String textContent) {
        NodeList nodes = parent.getElementsByTagNameNS(xmlService.getNamespaceUri(), tagName);
        if (nodes.getLength() > 0) {
            nodes.item(0).setTextContent(textContent);
        } else {
            Element el = doc.createElementNS(xmlService.getNamespaceUri(), tagName);
            el.setTextContent(textContent);
            parent.appendChild(el);
        }
    }
}