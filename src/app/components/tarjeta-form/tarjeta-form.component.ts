import { Component } from '@angular/core';
import { TarjetaService, Tarjeta } from '../../services/tarjeta.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tarjeta-form',
  templateUrl: './tarjeta-form.component.html',
  standalone: true,
  imports: [FormsModule, CommonModule]
})
export class TarjetaFormComponent {
  tarjeta: Tarjeta = { id: 0, numero: '', titular: '', tipo: '', fechaVencimiento: '', saldo: 0 };
  constructor(private service: TarjetaService, private router: Router) {}

  generarNumero(): void {
    // Generate 16-digit number: first 6 are product id (e.g., 123456), rest random
    const productId = '123456';
    const randomDigits = Math.floor(Math.random() * 10000000).toString().padStart(10, '0');
    this.tarjeta.numero = productId + randomDigits;
  }

  guardar() {
    // Set fechaVencimiento to 3 years from now in mm/aaaa format
    const now = new Date();
    now.setFullYear(now.getFullYear() + 3);
    const month = (now.getMonth() + 1).toString().padStart(2, '0');
    const year = now.getFullYear().toString();
    this.tarjeta.fechaVencimiento = `${month}/${year}`;
    this.tarjeta.saldo = 0;

    const { id, ...data } = this.tarjeta;
    this.service.create(data as Tarjeta).subscribe(() => this.router.navigate(['/tarjetas']));
  }
}
