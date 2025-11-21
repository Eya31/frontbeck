export interface User {
  id: number;
  nom: string;
  email: string;
  role: RoleType;
}

export enum RoleType {
  CITOYEN = 'CITOYEN',
  TECHNICIEN = 'TECHNICIEN',
  CHEF_SERVICE = 'CHEF_SERVICE',
  ADMINISTRATEUR = 'ADMINISTRATEUR'
}

export interface LoginRequest {
  email: string;
  motDePasse: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  userId: number;
  nom: string;
  email: string;
  role: RoleType;
}

export interface RegisterRequest {
  nom: string;
  email: string;
  motDePasse: string;
  role: RoleType;
  adresse?: string;
  telephone?: string;
  departement?: string;
}
