export interface Fournisseur {
  id: number;
  nom: string;
  email: string;
  telephone: string;
  adresse: string;
}

export interface Equipement {
  id: number;
  type: string;
  etat: 'FONCTIONNEL' | 'DEFECTUEUX' | 'EN_MAINTENANCE';
  fournisseur?: Fournisseur;
  valeurAchat: number;
  localisation?: {
    latitude: number;
    longitude: number;
  };
}
