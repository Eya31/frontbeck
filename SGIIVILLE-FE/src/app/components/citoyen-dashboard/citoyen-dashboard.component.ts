import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemandeFormComponent } from '../demande-form/demande-form.component';
import { Router } from '@angular/router';
import { DemandeService } from '../../services/demande.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-citoyen-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DemandeFormComponent],
  templateUrl: './citoyen-dashboard.component.html',
  styleUrls: ['./citoyen-dashboard.component.css']
})
export class CitoyenDashboardComponent implements OnInit {
  demandes: any[] = [];
  showCreateForm = false;
  nouvelleDemande: any = {
    description: '',
    localisation: { latitude: 0, longitude: 0 }
  };

  constructor(
    private demandeService: DemandeService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMesDemandes();
  }

  loadMesDemandes(): void {
    this.demandeService.getAllDemandes().subscribe({
      next: (data) => this.demandes = data,
      error: (err) => console.error(err)
    });
  }

  creerDemande(): void {
    if (!this.nouvelleDemande.description) {
      alert('Description requise');
      return;
    }
    this.demandeService.createDemande(this.nouvelleDemande).subscribe({
      next: () => {
        alert('Demande créée');
        this.showCreateForm = false;
        this.loadMesDemandes();
      },
      error: (err) => alert('Erreur: ' + err.message)
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
