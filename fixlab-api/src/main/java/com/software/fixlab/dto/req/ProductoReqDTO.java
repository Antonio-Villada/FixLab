package com.software.fixlab.dto.req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductoReqDTO {
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String sku;
    private Integer categoriaId;
    private Integer tipoProductoId;
    private MultipartFile imagen; // <--- ESTO ES LA CLAVE
}