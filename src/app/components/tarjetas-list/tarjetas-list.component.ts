import { Component, OnInit } from '@angular/core';
import { TarjetaService, Tarjeta } from '../../services/tarjeta.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-tarjetas-list',
  templateUrl: './tarjetas-list.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class TarjetasListComponent implements OnInit {
  tarjetas: Tarjeta[] = [];
  constructor(private tarjetaService: TarjetaService) {}
  ngOnInit(): void { this.cargarTarjetas(); }
  cargarTarjetas(): void { this.tarjetaService.getAll().subscribe(data => this.tarjetas = data); }
}
