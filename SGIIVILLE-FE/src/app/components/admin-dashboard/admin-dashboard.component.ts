import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService, CreateTechnicienRequest, CreateChefServiceRequest } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  users: any[] = [];
  showCreateTechForm = false;
  showCreateChefForm = false;

  newTechnicien: CreateTechnicienRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    competences: []
  };

  newChef: CreateChefServiceRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    departement: ''
  };

  competenceInput = '';

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: (error) => {
        console.error('Erreur chargement utilisateurs:', error);
      }
    });
  }

  createTechnicien(): void {
    if (!this.newTechnicien.nom || !this.newTechnicien.email || !this.newTechnicien.motDePasse) {
      alert('Veuillez remplir tous les champs');
      return;
    }

    this.adminService.createTechnicien(this.newTechnicien).subscribe({
      next: () => {
        alert('Technicien créé avec succès');
        this.showCreateTechForm = false;
        this.resetTechForm();
        this.loadUsers();
      },
      error: (error) => {
        console.error('Erreur création technicien:', error);
        alert('Erreur lors de la création du technicien');
      }
    });
  }

  createChef(): void {
    if (!this.newChef.nom || !this.newChef.email || !this.newChef.motDePasse || !this.newChef.departement) {
      alert('Veuillez remplir tous les champs');
      return;
    }

    this.adminService.createChefService(this.newChef).subscribe({
      next: () => {
        alert('Chef de service créé avec succès');
        this.showCreateChefForm = false;
        this.resetChefForm();
        this.loadUsers();
      },
      error: (error) => {
        console.error('Erreur création chef:', error);
        alert('Erreur lors de la création du chef de service');
      }
    });
  }

  addCompetence(): void {
    if (this.competenceInput.trim()) {
      this.newTechnicien.competences.push(this.competenceInput.trim());
      this.competenceInput = '';
    }
  }

  removeCompetence(index: number): void {
    this.newTechnicien.competences.splice(index, 1);
  }

  deleteUser(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.adminService.deleteUser(id).subscribe({
        next: () => {
          alert('Utilisateur supprimé');
          this.loadUsers();
        },
        error: (error) => {
          console.error('Erreur suppression:', error);
          alert('Erreur lors de la suppression');
        }
      });
    }
  }

  resetTechForm(): void {
    this.newTechnicien = {
      nom: '',
      email: '',
      motDePasse: '',
      competences: []
    };
  }

  resetChefForm(): void {
    this.newChef = {
      nom: '',
      email: '',
      motDePasse: '',
      departement: ''
    };
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
