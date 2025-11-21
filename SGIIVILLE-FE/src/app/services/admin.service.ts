import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CreateTechnicienRequest {
  nom: string;
  email: string;
  motDePasse: string;
  competences: string[];
}

export interface CreateChefServiceRequest {
  nom: string;
  email: string;
  motDePasse: string;
  departement: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  getAllTechniciens(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/techniciens`);
  }

  createTechnicien(request: CreateTechnicienRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/technicien`, request);
  }

  createChefService(request: CreateChefServiceRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/chef-service`, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
  }
}
