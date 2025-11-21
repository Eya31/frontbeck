// src/app/services/map.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Demande } from '../models/demande.model';
import { Intervention } from '../models/intervention.model';

export interface MapData {
  demandes: Demande[];
  interventions: Intervention[];
}

@Injectable({
  providedIn: 'root'
})
export class MapService {
  private baseUrl = 'http://localhost:8080/api/map';

  constructor(private http: HttpClient) {}

  getMapData(): Observable<MapData> {
    return this.http.get<MapData>(`${this.baseUrl}/data`);
  }

  getDemandePoints(): Observable<Demande[]> {
    return this.http.get<Demande[]>(`${this.baseUrl}/demandes-points`);
  }

  getInterventionPoints(): Observable<Intervention[]> {
    return this.http.get<Intervention[]>(`${this.baseUrl}/interventions-points`);
  }
}
