package tn.SGII_Ville.entities;

import tn.SGII_Ville.model.enums.RoleType;

public class Citoyen extends Utilisateur {
    private String adresse;
    private String telephone;

    // Constructeurs
    public Citoyen() {
        super();
    }

    public Citoyen(int id, String nom, String email, String motDePasse, String adresse, String telephone) {
        super(id, nom, email, motDePasse, RoleType.CITOYEN);
        this.adresse = adresse;
        this.telephone = telephone;
    }

    // Getters et Setters
    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return "Citoyen{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
