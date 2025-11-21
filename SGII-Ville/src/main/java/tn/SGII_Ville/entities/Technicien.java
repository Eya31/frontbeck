package tn.SGII_Ville.entities;


import tn.SGII_Ville.model.enums.RoleType;
import java.util.ArrayList;
import java.util.List;

public class Technicien extends Utilisateur {
    private List<String> competences;
    private boolean disponibilite;

    // Constructeurs
    public Technicien() {
        super();
        this.competences = new ArrayList<>();
    }

    public Technicien(int id, String nom, String email, String motDePasse, List<String> competences, boolean disponibilite) {
        super(id, nom, email, motDePasse, RoleType.TECHNICIEN);
        this.competences = competences != null ? competences : new ArrayList<>();
        this.disponibilite = disponibilite;
    }

    // Getters et Setters
    public List<String> getCompetences() {
        return competences;
    }

    public void setCompetences(List<String> competences) {
        this.competences = competences;
    }

    public boolean isDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }

    @Override
    public String toString() {
        return "Technicien{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", competences=" + competences +
                ", disponibilite=" + disponibilite +
                '}';
    }
}
