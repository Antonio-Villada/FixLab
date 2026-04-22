package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancieroSnapshotDTO {
    private InformeMetadatosDTO meta;
    private Instant generadoEn;
    private Double valorInventarioActivos;
    private Double ventasPagadasPeriodo;
    private Double ventasEntregadasPeriodo;
    private long pedidosPagadosPeriodo;
}
