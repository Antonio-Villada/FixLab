package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRespuestaRespDTO {
    private String respuesta;
    /** Acción sugerida: ver_pedido, ver_productos, ver_factura, etc. */
    private String tipoAccion;
    /** Payload para la acción, ej: id de pedido. */
    private String payload;
}
