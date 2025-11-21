import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest, RoleType } from '../../models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerData: RegisterRequest = {
    nom: '',
    email: '',
    motDePasse: '',
    role: RoleType.CITOYEN,
    adresse: '',
    telephone: '',
    departement: ''
  };
  
  confirmPassword: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;
  
  roles = [
    { value: RoleType.CITOYEN, label: 'Citoyen' },
    { value: RoleType.TECHNICIEN, label: 'Technicien' },
    { value: RoleType.CHEF_SERVICE, label: 'Chef de Service' },
    { value: RoleType.ADMINISTRATEUR, label: 'Administrateur' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Validation
    if (!this.registerData.nom || !this.registerData.email || !this.registerData.motDePasse) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    if (this.registerData.motDePasse !== this.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    if (this.registerData.motDePasse.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    // Validation spécifique par rôle
    if (this.registerData.role === RoleType.CITOYEN) {
      if (!this.registerData.adresse || !this.registerData.telephone) {
        this.errorMessage = 'Adresse et téléphone sont obligatoires pour un citoyen';
        return;
      }
    }

    if (this.registerData.role === RoleType.CHEF_SERVICE) {
      if (!this.registerData.departement) {
        this.errorMessage = 'Le département est obligatoire pour un chef de service';
        return;
      }
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        console.log('Inscription réussie', response);
        this.router.navigate(['/home']);
      },
      error: (error) => {
        console.error('Erreur d\'inscription', error);
        this.errorMessage = error.error?.message || 'Erreur lors de l\'inscription';
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  get isCitoyen(): boolean {
    return this.registerData.role === RoleType.CITOYEN;
  }

  get isChefService(): boolean {
    return this.registerData.role === RoleType.CHEF_SERVICE;
  }
}
