import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemandeService } from '../../services/demande.service';
import { EquipementService } from '../../services/equipement.service';
import { InterventionService } from '../../services/intervention.service';
import { RessourceService } from '../../services/ressource.service';
import { Demande } from '../../models/demande.model';
import { Equipement } from '../../models/equipement.model';
import { Intervention } from '../../models/intervention.model';
import { RessourceMaterielle } from '../../models/ressource.model';
import { Technicien } from '../../models/technicien.model';
import { TechnicienListService } from '../../services/technicien-list.service';
@Component({
  selector: 'app-chef-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chef-dashboard.component.html',
  styleUrls: ['./chef-dashboard.component.css']
})
export class ChefDashboardComponent implements OnInit {
  showEquipementForm = false;
techniciens: Technicien[] = [];
  showTechniciensModal = false;
  demandes: Demande[] = [];
  demandesFiltrees: Demande[] = [];
  equipements: Equipement[] = [];
  interventions: Intervention[] = [];
  ressources: RessourceMaterielle[] = [];

  // Stats
  demandesPendantes = 0;
  interventionsEnCours = 0;
  demandesTraitees = 0;

  // Modals
  showEquipementModal = false;
  showInterventionModal = false;
  showFormModal = false;
  showRessourceModal = false;
  showDetailModal = false;
  selectedDemande: Demande | null = null;
  editingEquipement: Equipement | null = null;
  editingRessource: RessourceMaterielle | null = null;

  currentEquipement: Equipement = {
    id: 0,
    type: '',
    etat: 'FONCTIONNEL',
    valeurAchat: 0,
    localisation: { latitude: 36.8065, longitude: 10.1815 }
  };

  currentRessource: RessourceMaterielle = {
    id: 0,
    designation: '',
    quantiteEnStock: 0,
    valeurAchat: 0
  } as RessourceMaterielle;

  filtreActif: 'TOUS' | 'NON_TRAITEES' | 'TRAITEES' = 'TOUS';

  constructor(
    private demandeService: DemandeService,
    private equipementService: EquipementService,
    private interventionService: InterventionService,
    private ressourceService: RessourceService,
    private technicienListService: TechnicienListService
  ) {}

  ngOnInit(): void {
    this.loadAllData();
    this.loadTechniciens(); // Charge dès l'ouverture
  }
loadTechniciens(): void {
    this.technicienListService.getAllTechniciens().subscribe({
      next: (data) => {
        this.techniciens = data;
      },
      error: (err) => {
        console.error('Erreur chargement techniciens:', err);
        alert('Impossible de charger la liste des techniciens');
      }
    });
  }
  openTechniciensModal(): void {
    this.showTechniciensModal = true;
    this.loadTechniciens(); // recharge à chaque ouverture
  }


  loadAllData(): void {
    this.loadDemandes();
    this.loadEquipements();
    this.loadRessources();
    this.loadInterventions();
  }

  loadDemandes(): void {
    this.demandeService.getAllDemandes().subscribe({
      next: (data) => {
        this.demandes = data;
        this.appliquerFiltre();
        this.updateStats();
      },
      error: (error) => {
        console.error('Erreur chargement demandes:', error);
        alert('Erreur lors du chargement des demandes');
      }
    });
  }

  loadEquipements(): void {
    this.equipementService.getAllEquipements().subscribe({
      next: (data) => this.equipements = data,
      error: (error) => {
        console.error('Erreur chargement équipements:', error);
        alert('Erreur lors du chargement des équipements');
      }
    });
  }

  loadInterventions(): void {
    this.interventionService.getAllInterventions().subscribe({
      next: (data) => this.interventions = data,
      error: (error) => {
        console.error('Erreur chargement interventions:', error);
        alert('Erreur lors du chargement des interventions');
      }
    });
  }

  loadRessources(): void {
    this.ressourceService.getAll().subscribe({
      next: (data) => this.ressources = data,
      error: (error) => {
        console.error('Erreur chargement matériels:', error);
      }
    });
  }

  updateStats(): void {
    this.demandesPendantes = this.demandes.filter(d =>
      d.etat === 'SOUMISE' || d.etat === 'EN_ATTENTE'
    ).length;
    this.demandesTraitees = this.demandes.filter(d => d.etat === 'TRAITEE').length;
    this.interventionsEnCours = this.interventions.filter(i =>
      i.etat === 'EN_ATTENTE' || i.etat === 'EN_COURS'
    ).length;
  }

  filtrerDemandes(filtre: 'TOUS' | 'NON_TRAITEES' | 'TRAITEES'): void {
    this.filtreActif = filtre;
    this.appliquerFiltre();
  }

  private appliquerFiltre(): void {
    switch (this.filtreActif) {
      case 'NON_TRAITEES':
        this.demandesFiltrees = this.demandes.filter(d =>
          d.etat === 'SOUMISE' || d.etat === 'EN_ATTENTE'
        );
        break;
      case 'TRAITEES':
        this.demandesFiltrees = this.demandes.filter(d => d.etat === 'TRAITEE');
        break;
      default:
        this.demandesFiltrees = this.demandes;
    }
  }

  planifierDemande(demande: Demande): void {
  if (demande.etat === 'TRAITEE') {
    alert('Cette demande est déjà planifiée !');
    return;
  }

  if (confirm(`Planifier l'intervention pour la demande #${demande.id} ?`)) {
    this.demandeService.planifierIntervention(demande.id).subscribe({
      next: (intervention) => {
        demande.etat = 'TRAITEE';  // Mise à jour immédiate dans l'interface
        this.updateStats();
        this.appliquerFiltre();
        this.loadInterventions();  // Recharge la liste des interventions
        alert('Intervention planifiée avec succès ! Intervention #' + intervention.id);
        this.closeDetailModal();
      },
      error: (error) => {
        console.error('Erreur complète:', error);

        // MESSAGE CLAIR AU LIEU DE [object Object]
        let message = 'Erreur lors de la planification';

        if (error?.error?.error) {
          message += ' : ' + error.error.error;
        } else if (error?.error?.details) {
          message += ' : ' + error.error.details;
        } else if (error?.error) {
          message += ' : ' + error.error;
        } else if (error?.message) {
          message += ' : ' + error.message;
        } else if (error?.status === 500) {
          message = 'Erreur serveur (500) – Contactez l’administrateur';
        }

        alert(message);
      }
    });
  }
}

  openAddEquipement() {
    this.editingEquipement = null;
    this.showEquipementForm = true;
  }

  editEquipement(equipement: any): void {
    this.editingEquipement = equipement;
    this.showEquipementForm = true;
  }

  // === DÉTAILS DEMANDE ===
  openDetailModal(demande: Demande): void {
    this.selectedDemande = demande;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedDemande = null;
  }

  // === GESTION ÉQUIPEMENTS ===
  openEquipementsModal(): void {
    this.showEquipementModal = true;
    this.loadEquipements();
  }

  openInterventionsModal(): void {
    this.showInterventionModal = true;
    this.loadInterventions();
  }

  // === GESTION RESSOURCES (MATERIELS) ===
  openRessourcesModal(): void {
    this.showRessourceModal = true;
    this.loadRessources();
  }

  openAddRessource(): void {
    this.editingRessource = null;
    this.resetRessourceForm();
    this.showFormModal = true;
  }

  editRessource(r: RessourceMaterielle): void {
    this.editingRessource = r;
    this.currentRessource = { ...r };
    this.showFormModal = true;
  }

  deleteRessource(id: number): void {
    if (!confirm('Supprimer ce matériel ?')) return;
    this.ressourceService.delete(id).subscribe({
      next: () => {
        this.ressources = this.ressources.filter(x => x.id !== id);
        alert('Matériel supprimé');
      },
      error: (error) => {
        console.error('Erreur suppression matériel:', error);
        alert('Erreur lors de la suppression');
      }
    });
  }

  closeModal(): void {
        this.showTechniciensModal = false;

    this.showEquipementModal = false;
    this.showInterventionModal = false;
    this.showFormModal = false;
    this.showRessourceModal = false;
    this.showDetailModal = false;
    this.editingEquipement = null;
    this.editingRessource = null;
    this.selectedDemande = null;
    this.resetForm();
  }

  private resetForm(): void {
    this.currentEquipement = {
      id: 0,
      type: '',
      etat: 'FONCTIONNEL',
      valeurAchat: 0,
      localisation: { latitude: 36.8065, longitude: 10.1815 }
    };
    this.resetRessourceForm();
  }

  private resetRessourceForm(): void {
    this.currentRessource = {
      id: 0,
      designation: '',
      quantiteEnStock: 0,
      valeurAchat: 0
    } as RessourceMaterielle;
  }

  saveEquipement(): void {
    if (!this.currentEquipement.type) {
      alert('Veuillez remplir le type d\'équipement');
      return;
    }

    const equipementData = {
      ...this.currentEquipement,
      fournisseur: this.currentEquipement.fournisseur || {
        id: 1,
        nom: 'Fournisseur Standard',
        email: 'contact@fournisseur.tn',
        telephone: '+216 70 000 000',
        adresse: ''
      }
    };

    const action = this.editingEquipement
      ? this.equipementService.updateEquipement(this.editingEquipement.id, equipementData)
      : this.equipementService.createEquipement(equipementData);

    action.subscribe({
      next: () => {
        alert(this.editingEquipement ? 'Équipement modifié !' : 'Équipement ajouté !');
        this.closeModal();
        this.loadEquipements();
      },
      error: (error) => {
        console.error('Erreur sauvegarde équipement:', error);
        alert('Erreur lors de la sauvegarde');
      }
    });
  }

  saveRessource(): void {
    const payload = { ...this.currentRessource } as RessourceMaterielle;
    if (this.editingRessource) {
      this.ressourceService.update(this.editingRessource.id, payload).subscribe({
        next: () => { this.loadRessources(); this.closeModal(); alert('Matériel modifié !'); },
        error: (error) => { console.error(error); alert('Erreur lors de la sauvegarde'); }
      });
    } else {
      this.ressourceService.create(payload).subscribe({
        next: () => { this.loadRessources(); this.closeModal(); alert('Matériel ajouté !'); },
        error: (error) => { console.error(error); alert('Erreur lors de la sauvegarde'); }
      });
    }
  }

  // Open photo in a new browser tab (simple viewer fallback)
  openPhotoModal(url: string, name?: string): void {
    const fullUrl = url.startsWith('http') ? url : 'http://localhost:8080' + url;
    window.open(fullUrl, '_blank');
  }

  deleteEquipement(id: number): void {
    if (confirm('Supprimer cet équipement ?')) {
      this.equipementService.deleteEquipement(id).subscribe({
        next: () => { this.equipements = this.equipements.filter(e => e.id !== id); alert('Équipement supprimé'); },
        error: (error) => { console.error('Erreur suppression:', error); alert('Erreur lors de la suppression'); }
      });
    }
  }

  refreshAll(): void {
    this.loadAllData();
    alert('Données actualisées !');
  }

  exportReport(): void {
    alert('Fonctionnalité export PDF en cours de développement...');
  }

  // Helper pour afficher les états
  getEtatBadgeClass(etat: string): string {
    switch (etat) {
      case 'SOUMISE':
      case 'EN_ATTENTE':
        return 'status pending';
      case 'TRAITEE':
        return 'status done';
      case 'FONCTIONNEL':
        return 'status done';
      case 'DEFECTUEUX':
        return 'status error';
      case 'EN_MAINTENANCE':
        return 'status warning';
      default:
        return 'status pending';
    }
  }

  getEtatText(etat: string): string {
    switch (etat) {
      case 'SOUMISE': return 'En attente';
      case 'EN_ATTENTE': return 'En attente';
      case 'TRAITEE': return 'Planifiée';
      case 'FONCTIONNEL': return 'Fonctionnel';
      case 'DEFECTUEUX': return 'Défectueux';
      case 'EN_MAINTENANCE': return 'En maintenance';
      default: return etat;
    }
  }
}
