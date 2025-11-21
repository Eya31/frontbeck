package tn.SGII_Ville.entities;

import tn.SGII_Ville.model.enums.RoleType;

public abstract class Utilisateur {
    private int id;
    private String nom;
    private String email;
    private String motDePasse;
    private RoleType role;

    // Constructeurs
    public Utilisateur() {}

    public Utilisateur(int id, String nom, String email, String motDePasse, RoleType role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}


