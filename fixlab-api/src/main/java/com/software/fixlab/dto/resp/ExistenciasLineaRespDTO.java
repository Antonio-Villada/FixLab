package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExistenciasLineaRespDTO {
    private String sku;
    private String nombre;
    private String categoriaNombre;
    private String tipoProductoNombre;
    private Boolean activo;
    private Integer stock;
    private Integer stockMinimo;
    private Double precioUnitario;
    private Double valorExistencia;
    private Boolean stockBajo;
    /** Texto corto para listados: OK, BAJO, Inactivo */
    private String estadoExistencia;
}
