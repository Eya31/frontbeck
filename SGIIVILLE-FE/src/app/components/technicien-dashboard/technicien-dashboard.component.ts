import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TechnicienService } from '../../services/technicien.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-technicien-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './technicien-dashboard.component.html',
  styleUrls: ['./technicien-dashboard.component.css']
})
export class TechnicienDashboardComponent implements OnInit {
  interventions: any[] = [];

  constructor(
    private technicienService: TechnicienService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMyInterventions();
  }

  loadMyInterventions(): void {
    this.technicienService.getMyInterventions().subscribe({
      next: (data) => this.interventions = data,
      error: (err) => console.error('Erreur:', err)
    });
  }

  terminer(id: number): void {
    if (confirm('Marquer cette intervention comme terminée?')) {
      this.technicienService.terminerIntervention(id).subscribe({
        next: () => {
          alert('Intervention terminée');
          this.loadMyInterventions();
        },
        error: (err) => alert('Erreur: ' + err.message)
      });
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
