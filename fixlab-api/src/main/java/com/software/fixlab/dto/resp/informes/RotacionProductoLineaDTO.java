package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RotacionProductoLineaDTO {
    private Long productoId;
    private String sku;
    private String nombre;
    private String categoriaNombre;
    private Integer unidadesVendidasPeriodo;
    private Integer stockActual;
    /** unidadesVendidas / max(stockActual, 1) en el periodo (aprox.). */
    private Double indiceRotacion;
}
