export interface Intervention {
  id: number;
  priorite: 'URGENTE' | 'PLANIFIEE';
  etat: 'EN_ATTENTE' | 'EN_COURS' | 'SUSPENDUE' | 'TERMINEE';
  datePlanifiee: string;
  budget: number;
  technicienId?: number;
  demandeId?: number; // Ajout de ce champ
}
