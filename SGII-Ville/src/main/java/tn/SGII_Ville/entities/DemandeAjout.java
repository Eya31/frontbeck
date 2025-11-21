package tn.SGII_Ville.entities;


import tn.SGII_Ville.model.enums.EtatDemandeAjoutType;
import java.time.LocalDate;

public class DemandeAjout {
    private int id;
    private String typeObjet;
    private int idChefService;
    private EtatDemandeAjoutType etat;
    private LocalDate dateSoumission;

    // Constructeurs
    public DemandeAjout() {}

    public DemandeAjout(int id, String typeObjet, int idChefService, 
                       EtatDemandeAjoutType etat, LocalDate dateSoumission) {
        this.id = id;
        this.typeObjet = typeObjet;
        this.idChefService = idChefService;
        this.etat = etat;
        this.dateSoumission = dateSoumission;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeObjet() {
        return typeObjet;
    }

    public void setTypeObjet(String typeObjet) {
        this.typeObjet = typeObjet;
    }

    public int getIdChefService() {
        return idChefService;
    }

    public void setIdChefService(int idChefService) {
        this.idChefService = idChefService;
    }

    public EtatDemandeAjoutType getEtat() {
        return etat;
    }

    public void setEtat(EtatDemandeAjoutType etat) {
        this.etat = etat;
    }

    public LocalDate getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDate dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    @Override
    public String toString() {
        return "DemandeAjout{" +
                "id=" + id +
                ", typeObjet='" + typeObjet + '\'' +
                ", idChefService=" + idChefService +
                ", etat=" + etat +
                ", dateSoumission=" + dateSoumission +
                '}';
    }
}