package tn.SGII_Ville.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tn.SGII_Ville.entities.Photo;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
    private final long MAX_FILE_SIZE = 8L * 1024L * 1024L; // 8MB
    private final int MAX_FILES = 5;

    // Types autorisés : image/* et audio/*
    private final List<String> allowedPrefixes = List.of("image/", "audio/");

    @Autowired
    private PhotoXmlService photoXmlService;

    public FileStorageService() throws IOException {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new IOException("Unable to create upload directory: " + uploadDir.toString(), e);
        }
    }

    /**
     * Stocke plusieurs fichiers et enregistre leurs métadonnées dans photos.xml
     */
    public List<Photo> storeFiles(MultipartFile[] files) throws Exception {

        // 1) Vérifications globales
        if (files == null || files.length == 0) return new ArrayList<>();

        if (files.length > MAX_FILES)
            throw new IllegalArgumentException("Max 5 fichiers");

        List<Photo> photos = new ArrayList<>();

        // 2) Traitement de chaque fichier
        for (MultipartFile file : files) {

            // Limite taille
            if (file.getSize() > MAX_FILE_SIZE)
                throw new IllegalArgumentException("Fichier trop grand: " + file.getOriginalFilename());

            // Vérification type
            String contentType = file.getContentType();
            if (contentType == null || allowedPrefixes.stream().noneMatch(contentType::startsWith))
                throw new IllegalArgumentException("Type non autorisé: " + contentType);

            // Nom original sécurisé
            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) original = "file";

            String base = original
                    .replaceAll("[\\\\/:*?\"<>|]", "_")
                    .replaceAll("\\s+", "_");

            if (base.length() > 100)
                base = base.substring(base.length() - 100);

            String safeName = System.currentTimeMillis() + "_" + base;
            Path target = uploadDir.resolve(safeName).normalize();

            // Sauvegarde sur disque
            try {
                if (target.getParent() != null && !Files.exists(target.getParent())) {
                    Files.createDirectories(target.getParent());
                }

                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            } catch (IOException ioe) {
                throw new IOException("Failed to store file '" + safeName + "' to " + target.toString(), ioe);
            }

            // Création de l’objet Photo pour XML
            Photo photo = new Photo();
            photo.setNom(original);
            photo.setUrl("/api/demandes/uploads/" + safeName);

            photos.add(photo);
        }

        // 3) Sauvegarde dans photos.xml (attribution id_photo)
        return photoXmlService.saveAll(photos);
    }

    /**
     * Retourne le chemin complet d’un fichier si valide et existant.
     */
    public Path getFilePath(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) return null;

        Path p = uploadDir.resolve(filename).normalize();

        if (!p.startsWith(uploadDir) || !Files.exists(p)) return null;

        return p;
    }
}
