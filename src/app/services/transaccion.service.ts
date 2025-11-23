import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { Transaccion } from '../models/transaccion.model';
export type { Transaccion } from '../models/transaccion.model';

@Injectable({ providedIn: 'root' })
export class TransaccionService {
  private baseUrl = '/api/transacciones';
  constructor(private http: HttpClient) {}
  getAll(): Observable<Transaccion[]> { return this.http.get<Transaccion[]>(this.baseUrl); }
  create(tx: Transaccion): Observable<Transaccion> { return this.http.post<Transaccion>(this.baseUrl, tx); }
  compra(compraData: any): Observable<any> { return this.http.post(`${this.baseUrl}/compra`, compraData); }
}
