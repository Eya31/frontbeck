import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InterventionService } from '../../services/intervention.service';
import { Intervention } from '../../models/intervention.model';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-intervention-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './intervention-list.component.html',
  styleUrls: ['./intervention-list.component.css']
})
export class InterventionListComponent implements OnInit, OnDestroy {
  interventions: Intervention[] = [];
  private pollingSub?: Subscription;

  constructor(private interventionService: InterventionService) {}

  ngOnInit(): void {
    this.loadInterventions();
    this.pollingSub = interval(5000).pipe(
      switchMap(() => this.interventionService.getAllInterventions())
    ).subscribe({
      next: data => this.interventions = data,
      error: (err) => console.error('Erreur polling interventions:', err)
    });
  }

  ngOnDestroy(): void {
    this.pollingSub?.unsubscribe();
  }

  loadInterventions(): void {
    this.interventionService.getAllInterventions().subscribe({
      next: (data) => this.interventions = data,
      error: (err) => console.error('Erreur chargement interventions:', err)
    });
  }

  changerStatut(id: number, statut: string): void {
    this.interventionService.updateStatut(id, statut).subscribe({
      next: updated => {
        const idx = this.interventions.findIndex(i => i.id === id);
        if (idx !== -1) this.interventions[idx] = updated;
      },
      error: (err) => {
        console.error('Erreur mise à jour statut:', err);
        alert('Erreur lors de la mise à jour du statut');
      }
    });
  }

  getPrioriteClass(priorite: string): string {
    const map: Record<string, string> = {
      'URGENTE': 'badge-urgent',
      'PLANIFIEE': 'badge-planned'
    };
    return map[priorite] || 'badge-default';
  }

  getStatutClass(statut: string): string {
    const map: Record<string, string> = {
      'EN_ATTENTE': 'status-pending',
      'EN_COURS': 'status-in-progress',
      'TERMINEE': 'status-completed',
      'SUSPENDUE': 'status-suspended'
    };
    return map[statut] || 'status-default';
  }
}
