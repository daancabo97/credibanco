import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { Tarjeta } from '../models/tarjeta.model';
export type { Tarjeta } from '../models/tarjeta.model';

@Injectable({ providedIn: 'root' })
export class TarjetaService {
  private baseUrl = '/api/tarjetas';
  constructor(private http: HttpClient) {}
  getAll(): Observable<Tarjeta[]> { return this.http.get<Tarjeta[]>(this.baseUrl); }
  create(tarjeta: Tarjeta): Observable<Tarjeta> { return this.http.post<Tarjeta>(this.baseUrl, tarjeta); }
}
