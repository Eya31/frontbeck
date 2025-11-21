import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemandeService } from '../../services/demande.service';
import { Demande } from '../../models/demande.model';

@Component({
  selector: 'app-demande-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demande-list.component.html',
  styleUrls: ['./demande-list.component.css']
})
export class DemandeListComponent implements OnInit {
  demandes: Demande[] = [];
  selectedDemande: Demande | null = null;
  showForm = false;
  showDetails = false;

  constructor(private demandeService: DemandeService) {}

  ngOnInit(): void {
    this.loadDemandes();
  }

  loadDemandes(): void {
    this.demandeService.getAllDemandes().subscribe({
      next: data => this.demandes = data,
      error: err => {
        console.error('Erreur chargement demandes:', err);
        this.demandes = [];
      }
    });
  }

  planifier(demande: Demande): void {
    this.selectedDemande = demande;
    this.showForm = true;
  }

  voirDetails(demande: Demande): void {
    this.selectedDemande = demande;
    this.showDetails = true;
    // Optionnel : Fetch photos ici si attachments sont IDs
    // Ex. : this.demandeService.getPhotosForDemande(demande.id).subscribe(photos => this.selectedDemande.photos = photos);
  }

  submitPlanification(): void {
    if (!this.selectedDemande) return;

    this.demandeService.planifierIntervention(this.selectedDemande.id)
      .subscribe({
        next: () => {
          this.selectedDemande!.etat = 'TRAITEE';
          this.showForm = false;
          this.selectedDemande = null;
          this.loadDemandes();
        },
        error: err => {
          console.error('Erreur planification:', err);
          alert('Échec de la planification : ' + (err?.error?.message || err.message || 'Erreur inconnue'));
        }
      });
  }

  annuler(): void {
    this.showForm = false;
    this.selectedDemande = null;
  }

  fermerDetails(): void {
    this.showDetails = false;
    this.selectedDemande = null;
  }
}
