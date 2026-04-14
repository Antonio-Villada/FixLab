package com.software.fixlab.dto.req;

import com.software.fixlab.entity.EstadoSolicitudPqr;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPqrCambiarEstadoReqDTO {

    @NotNull
    private EstadoSolicitudPqr nuevoEstado;

    /** Texto incluido en el correo al cliente (sin datos sensibles de terceros). */
    @Size(max = 2000)
    private String mensajeParaCliente;

    /** Nota interna (no se envía por correo). */
    @Size(max = 4000)
    private String notasInternas;
}
