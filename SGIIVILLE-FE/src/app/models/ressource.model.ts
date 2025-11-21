export interface RessourceMaterielle {
  id: number;
  designation: string;
  quantiteEnStock: number;
  valeurAchat: number;
  fournisseur?: {
    id: number;
    nom: string;
    email: string;
    telephone: string;
    adresse: string;
  };
}
