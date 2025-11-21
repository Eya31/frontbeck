package tn.SGII_Ville.entities;


import java.math.BigDecimal;

public class RessourceMaterielle {
    private int id;
    private String designation;
    private int quantiteEnStock;
    private BigDecimal valeurAchat;
    private Fournisseur fournisseur;

    // Constructeurs
    public RessourceMaterielle() {}

    public RessourceMaterielle(int id, String designation, int quantiteEnStock, 
                              BigDecimal valeurAchat, Fournisseur fournisseur) {
        this.id = id;
        this.designation = designation;
        this.quantiteEnStock = quantiteEnStock;
        this.valeurAchat = valeurAchat;
        this.fournisseur = fournisseur;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getQuantiteEnStock() {
        return quantiteEnStock;
    }

    public void setQuantiteEnStock(int quantiteEnStock) {
        this.quantiteEnStock = quantiteEnStock;
    }

    public BigDecimal getValeurAchat() {
        return valeurAchat;
    }

    public void setValeurAchat(BigDecimal valeurAchat) {
        this.valeurAchat = valeurAchat;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    @Override
    public String toString() {
        return "RessourceMaterielle{" +
                "id=" + id +
                ", designation='" + designation + '\'' +
                ", quantiteEnStock=" + quantiteEnStock +
                ", valeurAchat=" + valeurAchat +
                '}';
    }
}
