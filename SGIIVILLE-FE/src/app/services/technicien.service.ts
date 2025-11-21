import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Intervention } from '../models/intervention.model';

@Injectable({
  providedIn: 'root'
})
export class TechnicienService {
  private apiUrl = `${environment.apiUrl}/technicien`;

  constructor(private http: HttpClient) {}

  getMyInterventions(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(`${this.apiUrl}/interventions`);
  }

  terminerIntervention(id: number): Observable<Intervention> {
    return this.http.patch<Intervention>(`${this.apiUrl}/interventions/${id}/terminer`, {});
  }
}
