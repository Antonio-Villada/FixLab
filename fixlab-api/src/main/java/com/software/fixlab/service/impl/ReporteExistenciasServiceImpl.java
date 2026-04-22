package com.software.fixlab.service.impl;

import com.software.fixlab.dto.resp.ExistenciasLineaRespDTO;
import com.software.fixlab.dto.resp.ExistenciasReporteRespDTO;
import com.software.fixlab.dto.resp.ExistenciasResumenRespDTO;
import com.software.fixlab.entity.Producto;
import com.software.fixlab.repository.ProductoRepository;
import com.software.fixlab.service.interfaces.ReporteExistenciasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteExistenciasServiceImpl implements ReporteExistenciasService {

    private static final int STOCK_MINIMO_DEFECTO = 5;

    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public ExistenciasReporteRespDTO generarReporte() {
        List<Producto> productos = new ArrayList<>(productoRepository.findAll());
        productos.sort(Comparator
                .comparing((Producto p) -> p.getCategoria().getNombre(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Producto::getSku, String.CASE_INSENSITIVE_ORDER));

        Instant ahora = Instant.now();
        List<ExistenciasLineaRespDTO> lineas = new ArrayList<>();
        for (Producto p : productos) {
            lineas.add(mapearLinea(p));
        }

        ExistenciasResumenRespDTO resumen = construirResumen(ahora, productos, lineas);
        return ExistenciasReporteRespDTO.builder()
                .resumen(resumen)
                .lineas(lineas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReporteCsvUtf8() {
        ExistenciasReporteRespDTO reporte = generarReporte();
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("SKU,Nombre,Categoría,Tipo,Activo,Stock,Stock mínimo,Precio unitario,Valor existencia,Estado\n");
        for (ExistenciasLineaRespDTO l : reporte.getLineas()) {
            sb.append(csv(l.getSku()))
                    .append(',')
                    .append(csv(l.getNombre()))
                    .append(',')
                    .append(csv(l.getCategoriaNombre()))
                    .append(',')
                    .append(csv(l.getTipoProductoNombre()))
                    .append(',')
                    .append(Boolean.TRUE.equals(l.getActivo()) ? "Sí" : "No")
                    .append(',')
                    .append(l.getStock() != null ? l.getStock() : 0)
                    .append(',')
                    .append(l.getStockMinimo() != null ? l.getStockMinimo() : STOCK_MINIMO_DEFECTO)
                    .append(',')
                    .append(l.getPrecioUnitario() != null ? l.getPrecioUnitario() : 0.0)
                    .append(',')
                    .append(l.getValorExistencia() != null ? l.getValorExistencia() : 0.0)
                    .append(',')
                    .append(csv(l.getEstadoExistencia()))
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ExistenciasResumenRespDTO construirResumen(
            Instant fecha,
            List<Producto> productos,
            List<ExistenciasLineaRespDTO> lineas) {

        long activos = productos.stream().filter(p -> Boolean.TRUE.equals(p.getActivo())).count();
        long inactivos = productos.size() - activos;

        long totalUnidades = 0;
        long totalUnidadesActivos = 0;
        long bajo = 0;
        double valorActivos = 0.0;

        for (ExistenciasLineaRespDTO l : lineas) {
            int st = l.getStock() != null ? l.getStock() : 0;
            totalUnidades += st;
            if (Boolean.TRUE.equals(l.getActivo())) {
                totalUnidadesActivos += st;
                double vu = l.getPrecioUnitario() != null ? l.getPrecioUnitario() : 0.0;
                double ve = l.getValorExistencia() != null ? l.getValorExistencia() : st * vu;
                valorActivos += ve;
                if (Boolean.TRUE.equals(l.getStockBajo())) {
                    bajo++;
                }
            }
        }

        return ExistenciasResumenRespDTO.builder()
                .fechaGeneracion(fecha)
                .totalProductos(productos.size())
                .productosActivos(activos)
                .productosInactivos(inactivos)
                .totalUnidadesStock(totalUnidades)
                .totalUnidadesStockActivos(totalUnidadesActivos)
                .productosConStockBajo(bajo)
                .valorInventarioActivos(valorActivos)
                .build();
    }

    private ExistenciasLineaRespDTO mapearLinea(Producto p) {
        int stock = p.getStock() != null ? p.getStock() : 0;
        int min = p.getStockMinimo() != null ? p.getStockMinimo() : STOCK_MINIMO_DEFECTO;
        double precio = p.getPrecio() != null ? p.getPrecio() : 0.0;
        double valor = stock * precio;

        boolean activo = Boolean.TRUE.equals(p.getActivo());
        boolean bajo = activo && stock <= min;
        String estado;
        if (!activo) {
            estado = "Inactivo";
        } else if (bajo) {
            estado = "BAJO";
        } else {
            estado = "OK";
        }

        return ExistenciasLineaRespDTO.builder()
                .sku(p.getSku())
                .nombre(p.getNombre())
                .categoriaNombre(p.getCategoria().getNombre())
                .tipoProductoNombre(p.getTipoProducto().getNombre())
                .activo(activo)
                .stock(stock)
                .stockMinimo(min)
                .precioUnitario(precio)
                .valorExistencia(valor)
                .stockBajo(bajo)
                .estadoExistencia(estado)
                .build();
    }

    private static String csv(String raw) {
        if (raw == null) {
            return "\"\"";
        }
        String s = raw.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
