/** Pasos del flujo feliz (mismo orden que en backend). */
export const PASOS_FLUJO_REPARACION = [
  { estado: 'RECIBIDO', label: 'Recibido' },
  { estado: 'EN_DIAGNOSTICO', label: 'Diagnóstico' },
  { estado: 'COTIZADO_PENDIENTE_APROBACION', label: 'Cotización' },
  { estado: 'APROBADO', label: 'Aprobado' },
  { estado: 'EN_REPARACION', label: 'Reparación' },
  { estado: 'EN_PRUEBAS', label: 'Pruebas' },
  { estado: 'LISTO_ENTREGA', label: 'Listo entrega' },
  { estado: 'ENTREGADO', label: 'Entregado' },
] as const;

export type PasoFlujoReparacion = (typeof PASOS_FLUJO_REPARACION)[number];

export interface ProgresoReparacionVM {
  cancelado: boolean;
  /** Índice del paso actual en `PASOS_FLUJO_REPARACION`, o -1 si cancelado. */
  indice: number;
  total: number;
  /** 0–100 según avance en el flujo feliz. */
  porcentaje: number;
  pasos: readonly PasoFlujoReparacion[];
}

export function calcularProgresoReparacion(estadoRaw: string | null | undefined): ProgresoReparacionVM {
  const estado = String(estadoRaw || '')
    .trim()
    .toUpperCase();
  const pasos = PASOS_FLUJO_REPARACION;
  const total = pasos.length;
  if (estado === 'CANCELADO') {
    return { cancelado: true, indice: -1, total, porcentaje: 0, pasos };
  }
  const idx = pasos.findIndex((p) => p.estado === estado);
  const indice = idx >= 0 ? idx : 0;
  const porcentaje = Math.round(((indice + 1) / total) * 100);
  return { cancelado: false, indice, total, porcentaje, pasos };
}
