import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { calcularProgresoReparacion } from '../../models/reparacion-progreso';

@Component({
  selector: 'app-reparacion-progreso-estado',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reparacion-progreso-estado.html',
  styleUrl: './reparacion-progreso-estado.css',
})
export class ReparacionProgresoEstadoComponent {
  /** Estado actual (`EstadoReparacion` del API). */
  estado = input<string>('');
  /** Barra fina sin leyenda de pasos (tablas, cabeceras). */
  compact = input(false);

  readonly progreso = computed(() => calcularProgresoReparacion(this.estado()));
}
