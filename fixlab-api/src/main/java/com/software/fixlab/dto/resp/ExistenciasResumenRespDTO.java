package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExistenciasResumenRespDTO {
    private Instant fechaGeneracion;
    private long totalProductos;
    private long productosActivos;
    private long productosInactivos;
    /** Suma de unidades en stock (todos los productos). */
    private long totalUnidadesStock;
    /** Suma de unidades solo en productos activos. */
    private long totalUnidadesStockActivos;
    /** Productos activos con stock en o por debajo del mínimo. */
    private long productosConStockBajo;
    /** Valor aproximado: suma(stock × precio) solo productos activos. */
    private Double valorInventarioActivos;
}
