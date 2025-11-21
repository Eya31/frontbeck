package tn.SGII_Ville.entities;


import tn.SGII_Ville.model.enums.RoleType;

public class ChefDeService extends Utilisateur {
    private String departement;

    // Constructeurs
    public ChefDeService() {
        super();
    }

    public ChefDeService(int id, String nom, String email, String motDePasse, String departement) {
        super(id, nom, email, motDePasse, RoleType.CHEF_SERVICE);
        this.departement = departement;
    }

    // Getters et Setters
    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    @Override
    public String toString() {
        return "ChefDeService{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", departement='" + departement + '\'' +
                '}';
    }
}
