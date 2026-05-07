export type TipoSolicitudPqr = 'PETICION' | 'QUEJA' | 'RECLAMO' | 'SOLICITUD_GARANTIA' | 'SUGERENCIA';

export type EstadoSolicitudPqr = 'ABIERTO' | 'EN_ANALISIS' | 'RESUELTO' | 'CERRADO';

export type OrigenDocumentoPqr = 'FACTURA_PEDIDO' | 'TICKET_REPARACION' | 'SIN_REFERENCIA';

export interface SolicitudPqrRespDTO {
  id: number;
  radicado: string;
  tipo: TipoSolicitudPqr;
  estado: EstadoSolicitudPqr;
  origenDocumento: OrigenDocumentoPqr;
  pedidoId: number | null;
  reparacionId: number | null;
  reparacionNumeroTicket: string | null;
  descripcion: string;
  evidenciasUrls: string[];
  fechaRadicacion: string;
  fechaActualizacion: string;
  notasInternas: string | null;
  garantiaFisicaValidada: boolean;
  fechaValidacionGarantiaFisica: string | null;
  tecnicoValidacionCedula: string | null;
  tecnicoValidacionNombre: string | null;
  garantiaVigenteAlRadicar: boolean;
}

export interface SolicitudPqrCreateReqDTO {
  tipo: TipoSolicitudPqr;
  origenDocumento: OrigenDocumentoPqr;
  pedidoId?: number | null;
  reparacionId?: number | null;
  descripcion: string;
  evidenciasUrls: string[];
  consentimientoTratamientoDatos: boolean;
}

export interface SolicitudPqrCambiarEstadoReqDTO {
  nuevoEstado: EstadoSolicitudPqr;
  mensajeParaCliente?: string | null;
  notasInternas?: string | null;
}

export interface SolicitudPqrValidacionGarantiaReqDTO {
  garantiaFisicaValidada: boolean;
  notas?: string | null;
}

export const TIPOS_PQR: { value: TipoSolicitudPqr; label: string }[] = [
  { value: 'PETICION', label: 'Petición' },
  { value: 'QUEJA', label: 'Queja' },
  { value: 'RECLAMO', label: 'Reclamo' },
  { value: 'SUGERENCIA', label: 'Sugerencia' },
  { value: 'SOLICITUD_GARANTIA', label: 'Solicitud de garantía' },
];

export const ESTADOS_PQR: { value: EstadoSolicitudPqr; label: string }[] = [
  { value: 'ABIERTO', label: 'Abierto' },
  { value: 'EN_ANALISIS', label: 'En análisis' },
  { value: 'RESUELTO', label: 'Resuelto' },
  { value: 'CERRADO', label: 'Cerrado' },
];
