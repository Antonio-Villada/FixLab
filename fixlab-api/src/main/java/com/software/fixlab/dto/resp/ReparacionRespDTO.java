package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReparacionRespDTO {
    private Integer id;
    private String numeroTicket;
    private String estado;

    private EquipoRespDTO equipo;
    private Integer tallerId;
    private String tallerNombre;

    private String clienteCedula;
    private String clienteNombre;
    private String clienteApellido;

    private String tecnicoCedula;
    private String tecnicoNombre;
    private String tecnicoApellido;

    private String descripcionFalla;
    private String diagnostico;
    private Double cotizacionTotal;
    private LocalDateTime fechaDiagnostico;

    private boolean aprobadoCliente;
    private LocalDateTime fechaAprobacionCliente;

    private Integer mesesGarantiaServicio;
    private LocalDate fechaFinGarantiaServicio;

    private String notasInternas;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private List<ReparacionProductoLineRespDTO> lineasProducto;
    private List<ReparacionEvidenciaRespDTO> evidencias;
    private List<ReparacionHistorialEstadoRespDTO> historialEstados;
}
