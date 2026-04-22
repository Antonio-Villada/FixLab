package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepuestoTallerLineaDTO {
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private Long unidadesUsadas;
    private Double valorTotal;
}
