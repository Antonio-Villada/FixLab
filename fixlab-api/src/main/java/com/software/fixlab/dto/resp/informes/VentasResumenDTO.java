package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VentasResumenDTO {
    private InformeMetadatosDTO meta;
    private long totalPedidosPeriodo;
    private long pedidosPagados;
    private long pedidosEntregados;
    private long pedidosCancelados;
    private long pedidosOtrosEstados;
    private Double totalMontoPedidosPagados;
    private Double totalMontoPedidosEntregados;
    private Double totalMontoTodosEstados;
    private Double ticketPromedioPagados;
    /** Conteo por estado (clave = código de estado). */
    private Map<String, Long> pedidosPorEstado;
}
