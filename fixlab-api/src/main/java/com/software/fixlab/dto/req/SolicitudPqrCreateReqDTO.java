package com.software.fixlab.dto.req;

import com.software.fixlab.entity.OrigenDocumentoPqr;
import com.software.fixlab.entity.TipoSolicitudPqr;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPqrCreateReqDTO {

    @NotNull
    private TipoSolicitudPqr tipo;

    @NotNull
    private OrigenDocumentoPqr origenDocumento;

    private Integer pedidoId;

    private Integer reparacionId;

    @NotBlank
    @Size(max = 8000)
    private String descripcion;

    @Builder.Default
    private List<@Size(max = 1000) String> evidenciasUrls = new ArrayList<>();

    /**
     * Habeas Data (Ley 1581/2012, Colombia): el titular autoriza el tratamiento de datos personales
     * asociados a esta PQRS para gestión de la solicitud.
     */
    @NotNull
    @AssertTrue(message = "Debe aceptar el tratamiento de datos personales para radicar")
    private Boolean consentimientoTratamientoDatos;
}
