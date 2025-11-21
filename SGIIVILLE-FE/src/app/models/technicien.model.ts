// src/app/models/technicien.model.ts
export interface Technicien {
  id: number;
  nom: string;
  email: string;
  competences: string[];
  disponibilite: boolean;
}
