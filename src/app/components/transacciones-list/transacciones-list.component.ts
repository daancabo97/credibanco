import { Component, OnInit } from '@angular/core';
import { TransaccionService, Transaccion } from '../../services/transaccion.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-transacciones-list',
  templateUrl: './transacciones-list.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class TransaccionesListComponent implements OnInit {
  transacciones: Transaccion[] = [];
  constructor(private txService: TransaccionService) {}
  ngOnInit(): void { this.cargarTransacciones(); }
  cargarTransacciones(): void { this.txService.getAll().subscribe(data => this.transacciones = data); }
  anular(tx: Transaccion): void {
    if (confirm('¿Anular esta transacción?')) {
      this.txService.anular(tx.id).subscribe(() => this.cargarTransacciones());
    }
  }
}
