import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Intervention } from '../models/intervention.model';

@Injectable({ providedIn: 'root' })
export class InterventionService {
  private baseUrl = 'http://localhost:8080/api/interventions';

  constructor(private http: HttpClient) {}

  getAllInterventions(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(this.baseUrl);
  }

  getInterventionById(id: number): Observable<Intervention> {
    return this.http.get<Intervention>(`${this.baseUrl}/${id}`);
  }

  updateStatut(interventionId: number, statut: string): Observable<Intervention> {
    return this.http.patch<Intervention>(`${this.baseUrl}/${interventionId}`, { statut: statut });
  }

  affecterTechnicien(interventionId: number, technicienId: number): Observable<Intervention> {
    return this.http.put<Intervention>(`${this.baseUrl}/${interventionId}/affecter`, { technicienId });
  }
}
