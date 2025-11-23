import { Component, OnInit } from '@angular/core';
import { CartService, CartItem } from '../../services/cart.service';
import { TarjetaService, Tarjeta } from '../../services/tarjeta.service';
import { TransaccionService } from '../../services/transaccion.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class CheckoutComponent implements OnInit {
  cartItems: CartItem[] = [];
  total = 0;
  tarjetas: Tarjeta[] = [];
  selectedTarjetaId = '';
  nombreTitular = '';
  numeroTarjeta = '';
  fechaVencimiento = '';

  constructor(
    private cartService: CartService,
    private tarjetaService: TarjetaService,
    private transaccionService: TransaccionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartService.cartItems$.subscribe(items => {
      this.cartItems = items;
      this.total = this.cartService.getTotal();
    });
    this.tarjetaService.getAll().subscribe(tarjetas => this.tarjetas = tarjetas);
  }

  pagar(): void {
    if (!this.selectedTarjetaId || !this.nombreTitular || !this.numeroTarjeta || !this.fechaVencimiento) {
      alert('Por favor complete todos los campos');
      return;
    }

    const compraData = {
      tarjetaId: this.selectedTarjetaId,
      monto: this.total,
      descripcion: `Compra de ${this.cartItems.length} productos`
    };

    this.transaccionService.compra(compraData).subscribe({
      next: () => {
        alert('Compra realizada exitosamente');
        this.cartService.clearCart();
        this.router.navigate(['/']);
      },
      error: (err) => {
        alert('Error en la compra: ' + err.message);
      }
    });
  }
}
