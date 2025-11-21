package tn.SGII_Ville.entities;


import tn.SGII_Ville.common.PointGeo;
import tn.SGII_Ville.model.enums.EtatEquipementType;
import java.math.BigDecimal;

public class Equipement {
    private int id;
    private String type;
    private EtatEquipementType etat;
    private Fournisseur fournisseur;
    private BigDecimal valeurAchat;
    private PointGeo localisation;

    // Constructeurs
    public Equipement() {}

    public Equipement(int id, String type, EtatEquipementType etat, Fournisseur fournisseur, 
                     BigDecimal valeurAchat, PointGeo localisation) {
        this.id = id;
        this.type = type;
        this.etat = etat;
        this.fournisseur = fournisseur;
        this.valeurAchat = valeurAchat;
        this.localisation = localisation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EtatEquipementType getEtat() {
        return etat;
    }

    public void setEtat(EtatEquipementType etat) {
        this.etat = etat;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public BigDecimal getValeurAchat() {
        return valeurAchat;
    }

    public void setValeurAchat(BigDecimal valeurAchat) {
        this.valeurAchat = valeurAchat;
    }

    public PointGeo getLocalisation() {
        return localisation;
    }

    public void setLocalisation(PointGeo localisation) {
        this.localisation = localisation;
    }

    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", etat=" + etat +
                ", valeurAchat=" + valeurAchat +
                '}';
    }
}
