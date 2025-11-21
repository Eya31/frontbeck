package tn.SGII_Ville.entities;


import tn.SGII_Ville.model.enums.RoleType;

public class Administrateur extends Utilisateur {

    // Constructeurs
    public Administrateur() {
        super();
    }

    public Administrateur(int id, String nom, String email, String motDePasse) {
        super(id, nom, email, motDePasse, RoleType.ADMINISTRATEUR);
    }

    @Override
    public String toString() {
        return "Administrateur{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}