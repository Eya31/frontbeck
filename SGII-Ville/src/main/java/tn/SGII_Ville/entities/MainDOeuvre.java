package tn.SGII_Ville.entities;


public class MainDOeuvre {
    private int id;
    private String nom;
    private String cin;
    private String telephone;

    // Constructeurs
    public MainDOeuvre() {}

    public MainDOeuvre(int id, String nom, String cin, String telephone) {
        this.id = id;
        this.nom = nom;
        this.cin = cin;
        this.telephone = telephone;
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

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return "MainDOeuvre{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", cin='" + cin + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}