import { Routes } from '@angular/router';
import { TarjetasListComponent } from './components/tarjetas-list/tarjetas-list.component';
import { TarjetaFormComponent } from './components/tarjeta-form/tarjeta-form.component';
import { TransaccionesListComponent } from './components/transacciones-list/transacciones-list.component';
import { TransaccionFormComponent } from './components/transaccion-form/transaccion-form.component';

export const routes: Routes = [
  { path: 'tarjetas', component: TarjetasListComponent },
  { path: 'tarjetas/nuevo', component: TarjetaFormComponent },
  { path: 'transacciones', component: TransaccionesListComponent },
  { path: 'transacciones/nuevo', component: TransaccionFormComponent },
  { path: '', redirectTo: '/tarjetas', pathMatch: 'full' },
];
