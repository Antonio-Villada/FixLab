package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VentasPorCategoriaLineaDTO {
    private Integer categoriaId;
    private String categoriaNombre;
    private Long unidadesVendidas;
    private Double montoTotal;
    private Double participacionPorcentaje;
}
