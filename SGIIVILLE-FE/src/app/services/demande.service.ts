import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Demande } from '../models/demande.model';
import { catchError, throwError } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class DemandeService {
  private baseUrl = 'http://localhost:8080/api/demandes';

  constructor(private http: HttpClient) {}

  /** --------------------------------------------
   *  RÉCUPÉRER TOUTES LES DEMANDES
   * --------------------------------------------- */
  getAllDemandes(): Observable<Demande[]> {
    return this.http.get<Demande[]>(this.baseUrl);
  }

  /** --------------------------------------------
   *  RÉCUPÉRER UNE DEMANDE PAR ID
   * --------------------------------------------- */
  getDemandeById(id: number): Observable<Demande> {
    return this.http.get<Demande>(`${this.baseUrl}/${id}`);
  }

  /** --------------------------------------------
   *  CRÉER UNE DEMANDE (AVEC FILES OPTIONNELS)
   * --------------------------------------------- */
  createDemande(demande: Partial<Demande> | any, files?: File[]): Observable<any> {
    if (files && files.length > 0) {
      const fd = new FormData();
      fd.append('demande', JSON.stringify(demande));
      files.forEach(f => fd.append('files', f, f.name));
      return this.http.post<any>(this.baseUrl, fd);
    }
    return this.http.post<any>(this.baseUrl, demande);
  }

  /** --------------------------------------------
   * PLANIFIER UNE INTERVENTION
   * priorite = 'PLANIFIEE' | 'URGENTE'
   * --------------------------------------------- */
  planifierIntervention(demandeId: number): Observable<any> {
  return this.http.post<any>(`${this.baseUrl}/planifier/${demandeId}`, {}).pipe(
    catchError(error => {
      console.error('Erreur API planifier:', error);
      // Tu peux transformer l'erreur ici si besoin
      return throwError(() => error);
    })
  );
}

  /** --------------------------------------------
   * UPLOAD DE PHOTOS POUR UNE DEMANDE EXISTANTE
   * --------------------------------------------- */
  uploadPhotos(demandeId: number, files: File[]): Observable<any> {
    const fd = new FormData();
    files.forEach(f => fd.append('files', f, f.name));
    return this.http.post<any>(`${this.baseUrl}/${demandeId}/photos`, fd);
  }
}
