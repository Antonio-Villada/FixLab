package com.software.fixlab.dto.resp;

import com.software.fixlab.entity.EstadoSolicitudPqr;
import com.software.fixlab.entity.OrigenDocumentoPqr;
import com.software.fixlab.entity.TipoSolicitudPqr;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPqrRespDTO {

    private Long id;
    private String radicado;

    private TipoSolicitudPqr tipo;
    private EstadoSolicitudPqr estado;
    private OrigenDocumentoPqr origenDocumento;

    private Integer pedidoId;
    private Integer reparacionId;
    private String reparacionNumeroTicket;

    private String descripcion;

    @Builder.Default
    private List<String> evidenciasUrls = new ArrayList<>();

    private LocalDateTime fechaRadicacion;
    private LocalDateTime fechaActualizacion;

    /** Solo personal autorizado. */
    private String notasInternas;

    private boolean garantiaFisicaValidada;
    private LocalDateTime fechaValidacionGarantiaFisica;
    private String tecnicoValidacionCedula;
    private String tecnicoValidacionNombre;

    private boolean garantiaVigenteAlRadicar;
}
