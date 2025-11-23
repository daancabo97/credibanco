import { Component } from '@angular/core';
import { TransaccionService, Transaccion } from '../../services/transaccion.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-transaccion-form',
  templateUrl: './transaccion-form.component.html',
  standalone: true,
  imports: [FormsModule, CommonModule]
})
export class TransaccionFormComponent {
  // Usamos la interfaz para asegurar que el objeto tiene la forma correcta
  tx: Transaccion = { id: 0, tarjetaId: 0, monto: 0, descripcion: '' };
  constructor(private service: TransaccionService, private router: Router) {}
  guardar() {
    const { id, ...data } = this.tx;
    this.service.create(data as Transaccion).subscribe(() => this.router.navigate(['/transacciones']));
  }
}
