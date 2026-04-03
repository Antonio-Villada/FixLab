export interface TipoEquipoRespDTO {
  id: number;
  nombre: string;
  fechaCreacion?: string;
}

export interface TipoTallerRespDTO {
  id: number;
  nombre: string;
  ciclo?: string | null;
  estado?: string | null;
  fechaCreacion?: string;
}

export interface TallerRespDTO {
  id: number;
  nombre: string;
  tipoTallerId: number;
  tipoTallerNombre: string;
  fechaCreacion?: string;
  fechaActualizacion?: string | null;
}

export interface EquipoRespDTO {
  id: number;
  tipoEquipoId: number;
  tipoEquipoNombre: string;
  clienteCedula: string;
  clienteNombre: string;
  clienteApellido: string;
  marca?: string | null;
  modelo?: string | null;
  numeroSerie?: string | null;
  observaciones?: string | null;
  fechaCreacion?: string;
  fechaActualizacion?: string | null;
}

export interface ReparacionProductoLineRespDTO {
  id: number;
  productoId: number;
  nombreProducto: string;
  sku?: string;
  cantidad: number;
  precioUnitarioSnapshot: number;
  subtotal: number;
}

export interface ReparacionEvidenciaRespDTO {
  id: number;
  url: string;
  tipo: string;
  orden?: number | null;
  fechaCreacion?: string;
}

export interface ReparacionHistorialEstadoRespDTO {
  id: number;
  estadoAnterior?: string | null;
  estadoNuevo: string;
  usuarioCedula?: string | null;
  usuarioNombre?: string | null;
  usuarioApellido?: string | null;
  comentario?: string | null;
  fechaCambio?: string;
}

export interface EquipoReqDTO {
  tipoEquipoId: number;
  marca?: string;
  modelo?: string;
  numeroSerie?: string;
  observaciones?: string;
  /** Obligatorio para ADMIN y TECNICO: cédula del cliente dueño. */
  propietarioCedula?: string;
}

export interface ReparacionCreateReqDTO {
  equipoId: number;
  tallerId: number;
  descripcionFalla: string;
}

export interface ReparacionAsignarTecnicoReqDTO {
  tecnicoCedula: string;
}

export interface ReparacionDiagnosticoCotizacionReqDTO {
  diagnostico?: string;
  cotizacionTotal?: number | null;
}

export interface ReparacionCambiarEstadoReqDTO {
  estadoNuevo: string;
  comentario?: string;
}

export interface ReparacionProductoReqDTO {
  productoId: number;
  cantidad: number;
}

export interface ReparacionEvidenciaReqDTO {
  url: string;
  tipo: string;
  orden?: number | null;
}

/** Valores del enum backend `EstadoReparacion` (referencia completa). */
export const ESTADOS_REPARACION = [
  'RECIBIDO',
  'EN_DIAGNOSTICO',
  'COTIZADO_PENDIENTE_APROBACION',
  'APROBADO',
  'EN_REPARACION',
  'EN_PRUEBAS',
  'LISTO_ENTREGA',
  'ENTREGADO',
  'CANCELADO',
] as const;

/**
 * Estados que el taller puede aplicar con "Cambiar estado" (no incluye RECIBIDO ni APROBADO: ingreso en recepción; aprobación vía cliente).
 */
export const ESTADOS_REPARACION_CAMBIO_TALLER = [
  'EN_DIAGNOSTICO',
  'COTIZADO_PENDIENTE_APROBACION',
  'EN_REPARACION',
  'EN_PRUEBAS',
  'LISTO_ENTREGA',
  'ENTREGADO',
  'CANCELADO',
] as const;

export const TIPOS_EVIDENCIA_REPARACION = [
  'RECEPCION',
  'DIAGNOSTICO',
  'DURANTE_REPARACION',
  'POST_REPARACION',
] as const;

export interface ReparacionRespDTO {
  id: number;
  numeroTicket: string;
  estado: string;
  equipo: EquipoRespDTO;
  tallerId: number;
  tallerNombre: string;
  clienteCedula: string;
  clienteNombre: string;
  clienteApellido: string;
  tecnicoCedula?: string | null;
  tecnicoNombre?: string | null;
  tecnicoApellido?: string | null;
  descripcionFalla: string;
  diagnostico?: string | null;
  cotizacionTotal?: number | null;
  fechaDiagnostico?: string | null;
  aprobadoCliente: boolean;
  fechaAprobacionCliente?: string | null;
  mesesGarantiaServicio?: number | null;
  fechaFinGarantiaServicio?: string | null;
  notasInternas?: string | null;
  fechaCreacion?: string;
  fechaActualizacion?: string | null;
  lineasProducto?: ReparacionProductoLineRespDTO[] | null;
  evidencias?: ReparacionEvidenciaRespDTO[] | null;
  historialEstados?: ReparacionHistorialEstadoRespDTO[] | null;
}
