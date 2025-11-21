import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RessourceMaterielle } from '../models/ressource.model';

@Injectable({
  providedIn: 'root'
})
export class RessourceService {
private apiUrl = 'http://localhost:8080/api/ressources';

  constructor(private http: HttpClient) {}

  getAll(): Observable<RessourceMaterielle[]> {
    return this.http.get<RessourceMaterielle[]>(this.apiUrl);
  }

  getById(id: number): Observable<RessourceMaterielle> {
    return this.http.get<RessourceMaterielle>(`${this.apiUrl}/${id}`);
  }

  create(r: RessourceMaterielle): Observable<RessourceMaterielle> {
    return this.http.post<RessourceMaterielle>(this.apiUrl, r);
  }

  update(id: number, r: RessourceMaterielle): Observable<RessourceMaterielle> {
    return this.http.put<RessourceMaterielle>(`${this.apiUrl}/${id}`, r);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
