package tn.SGII_Ville.entities;

/**
 * Entité représentant une photo attachée à une demande
 */
public class Photo {
    private int idPhoto;
    private String url;
    private String nom;

    // Constructeurs
    public Photo() {}

    public Photo(int idPhoto, String url, String nom) {
        this.idPhoto = idPhoto;
        this.url = url;
        this.nom = nom;
    }

    // Getters et Setters
    public int getIdPhoto() {
        return idPhoto;
    }

    public void setIdPhoto(int idPhoto) {
        this.idPhoto = idPhoto;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "idPhoto=" + idPhoto +
                ", url='" + url + '\'' +
                ", nom='" + nom + '\'' +
                '}';
    }
}
