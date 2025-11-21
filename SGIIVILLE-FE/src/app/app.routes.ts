import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { DemandeListComponent } from './components/demande-list/demande-list.component';
import { InterventionListComponent } from './components/intervention-list/intervention-list.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { ChefDashboardComponent } from './components/chef-dashboard/chef-dashboard.component';        // ← déjà là
import { TechnicienDashboardComponent } from './components/technicien-dashboard/technicien-dashboard.component';
import { CitoyenDashboardComponent } from './components/citoyen-dashboard/citoyen-dashboard.component';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'intervention-list', component: ChefDashboardComponent, canActivate: [authGuard] },

  // DASHBOARDS PAR RÔLE – LE PLUS IMPORTANT
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard(['ADMINISTRATEUR'])]
  },
  {
    path: 'chef',
    component: ChefDashboardComponent,
    canActivate: [authGuard, roleGuard(['CHEF_SERVICE'])]
  },
  {
    path: 'technicien',
    component: TechnicienDashboardComponent,
    canActivate: [authGuard, roleGuard(['TECHNICIEN'])]
  },
  {
    path: 'citoyen',
    // ou 'dashboard' si tu préfères
    component: CitoyenDashboardComponent,
    canActivate: [authGuard, roleGuard(['CITOYEN'])]
  },

  // Routes classiques (gardées pour compatibilité)
  { path: 'demandes', component: DemandeListComponent, canActivate: [authGuard] },
  { path: 'interventions', component: InterventionListComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: '/login' }
];
