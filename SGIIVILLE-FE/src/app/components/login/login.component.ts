import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  credentials: LoginRequest = {
    email: '',
    motDePasse: ''
  };
  
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.credentials.email || !this.credentials.motDePasse) {
      this.errorMessage = 'Veuillez remplir tous les champs';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        console.log('Connexion réussie', response);
        // Redirection basée sur le rôle de l'utilisateur
        this.redirectBasedOnRole(response.role);
      },
      error: (error) => {
        console.error('Erreur de connexion', error);
        this.errorMessage = error.error?.message || 'Email ou mot de passe incorrect';
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private redirectBasedOnRole(role: string): void {
    switch(role) {
      case 'ADMINISTRATEUR':
        this.router.navigate(['/admin']);
        break;
      case 'CHEF_SERVICE':
        this.router.navigate(['/chef']);
        break;
      case 'TECHNICIEN':
        this.router.navigate(['/technicien']);
        break;
      case 'CITOYEN':
        this.router.navigate(['/citoyen']);
        break;
      default:
        this.router.navigate(['/home']);
    }
  }
}
