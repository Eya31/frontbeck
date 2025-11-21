// src/app/services/technicien-list.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Technicien } from '../models/technicien.model';

@Injectable({
  providedIn: 'root'
})
export class TechnicienListService {
  private apiUrl = `${environment.apiUrl}/api/chef/techniciens`; // BON CHEMIN

  constructor(private http: HttpClient) {}

  getAllTechniciens(): Observable<Technicien[]> {
    return this.http.get<Technicien[]>(this.apiUrl);
  }
}
