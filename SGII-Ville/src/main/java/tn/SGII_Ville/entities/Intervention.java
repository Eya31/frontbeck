package tn.SGII_Ville.entities;

import tn.SGII_Ville.model.enums.EtatInterventionType;
import tn.SGII_Ville.model.enums.PrioriteType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Intervention {
    private int id;
    private PrioriteType priorite;
    private EtatInterventionType etat;
    private LocalDate datePlanifiee;
    private BigDecimal budget;
    private int technicienId;
    private Integer demandeId;

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public PrioriteType getPriorite() { return priorite; }
    public void setPriorite(PrioriteType priorite) { this.priorite = priorite; }

    public EtatInterventionType getEtat() { return etat; }
    public void setEtat(EtatInterventionType etat) { this.etat = etat; }

    public LocalDate getDatePlanifiee() { return datePlanifiee; }
    public void setDatePlanifiee(LocalDate datePlanifiee) { this.datePlanifiee = datePlanifiee; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public int getTechnicienId() { return technicienId; }
    public void setTechnicienId(int technicienId) { this.technicienId = technicienId; }

    public Integer getDemandeId() { return demandeId; }
    public void setDemandeId(Integer demandeId) { this.demandeId = demandeId; }

    @Override
    public String toString() {
        return "Intervention{" +
                "id=" + id +
                ", priorite=" + priorite +
                ", etat=" + etat +
                ", datePlanifiee=" + datePlanifiee +
                ", budget=" + budget +
                ", technicienId=" + technicienId +
                ", demandeId=" + demandeId +
                '}';
    }
}