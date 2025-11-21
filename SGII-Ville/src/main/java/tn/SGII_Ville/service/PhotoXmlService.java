package tn.SGII_Ville.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tn.SGII_Ville.entities.Photo;

@Service
public class PhotoXmlService {

    @Autowired
    private XmlService xmlService;

    /**
     * Sauvegarde une photo metadata dans photos.xml
     */
    public Photo save(Photo photo) throws Exception {
        Document doc;

        try {
            doc = xmlService.loadXmlDocument("Photos");
        } catch (Exception e) {
            // créer le document si manquant
            doc = xmlService.createNewDocument("Photos");
        }

        Element root = doc.getDocumentElement();
        if (root == null) {
            root = doc.createElementNS(xmlService.getNamespaceUri(), "Photos");
            doc.appendChild(root);
        }

        // generate id unique
        int newId = xmlService.generateNewId(doc, "Photo");
        photo.setIdPhoto(newId);

        // création de l’élément Photo
        Element photoEl = xmlService.createElement(doc, "Photo");
        xmlService.addTextElement(doc, photoEl, "id_photo", String.valueOf(photo.getIdPhoto()));
        xmlService.addTextElement(doc, photoEl, "url", photo.getUrl());
        xmlService.addTextElement(doc, photoEl, "nom", photo.getNom());

        root.appendChild(photoEl);

        xmlService.saveXmlDocument(doc, "Photos");
        return photo;
    }

    /**
     * Sauvegarde plusieurs photos
     */
    public List<Photo> saveAll(List<Photo> photos) throws Exception {
        List<Photo> saved = new ArrayList<>();
        for (Photo p : photos) {
            saved.add(save(p));
        }
        return saved;
    }

    /**
     * Récupérer toutes les photos
     */
    public List<Photo> findAll() throws Exception {
        List<Photo> res = new ArrayList<>();

        Document doc = xmlService.loadXmlDocument("Photos");
        Element root = doc.getDocumentElement();
        if (root == null) return res;

        NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Photo");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);

            String id = xmlService.getElementTextContent(e, "id_photo");
            String url = xmlService.getElementTextContent(e, "url");
            String nom = xmlService.getElementTextContent(e, "nom");

            if (id != null) {
                Photo p = new Photo();
                p.setIdPhoto(Integer.parseInt(id));
                p.setUrl(url != null ? url : "");
                p.setNom(nom != null ? nom : "");
                res.add(p);
            }
        }
        return res;
    }

    /**
     * Find a photo by id.
     */
    public Photo findById(int id) {
        try {
            Document doc = xmlService.loadXmlDocument("Photos");
            Element root = doc.getDocumentElement();
            if (root == null) return null;

            NodeList nodes = root.getElementsByTagNameNS(xmlService.getNamespaceUri(), "Photo");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element e = (Element) nodes.item(i);

                String sid = xmlService.getElementTextContent(e, "id_photo");
                if (sid == null) continue;

                try {
                    int pid = Integer.parseInt(sid);

                    if (pid == id) {
                        String url = xmlService.getElementTextContent(e, "url");
                        String nom = xmlService.getElementTextContent(e, "nom");

                        Photo p = new Photo();
                        p.setIdPhoto(pid);
                        p.setUrl(url != null ? url : "");
                        p.setNom(nom != null ? nom : "");
                        return p;
                    }

                } catch (NumberFormatException ignore) {}
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
