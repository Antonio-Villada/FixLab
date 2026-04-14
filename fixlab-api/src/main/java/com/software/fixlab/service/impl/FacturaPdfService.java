package com.software.fixlab.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.software.fixlab.entity.DetallePedido;
import com.software.fixlab.entity.Pedido;
import com.software.fixlab.util.VentaTemplateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacturaPdfService {

    @Value("${fixlab.frontend.url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${fixlab.mail.logo-url:}")
    private String mailLogoUrl;

    public byte[] generarFacturaPdf(Pedido pedido, List<DetallePedido> detalles) {
        if (pedido == null) throw new IllegalArgumentException("pedido es null");

        String nombre = pedido.getCliente() != null
                ? ((pedido.getCliente().getNombre() != null ? pedido.getCliente().getNombre() : "") + " " +
                (pedido.getCliente().getApellido() != null ? pedido.getCliente().getApellido() : "")).trim()
                : "Cliente";
        String email = pedido.getCliente() != null ? pedido.getCliente().getEmail() : "";

        List<VentaTemplateUtil.VentaItemRow> items = new ArrayList<>();
        for (DetallePedido d : detalles != null ? detalles : List.<DetallePedido>of()) {
            String prod = (d.getProducto() != null && d.getProducto().getNombre() != null) ? d.getProducto().getNombre() : "Producto";
            int cant = d.getCantidad() != null ? d.getCantidad() : 0;
            double precio = d.getPrecioUnitario() != null ? d.getPrecioUnitario() : 0.0;
            items.add(new VentaTemplateUtil.VentaItemRow(prod, cant, precio));
        }

        String html = VentaTemplateUtil.renderFacturaHtml(
                nombre,
                getLogoUrl(),
                email,
                pedido.getDireccionEnvio(),
                String.valueOf(pedido.getId()),
                pedido.getFechaCreacion(),
                items,
                pedido.getTotal() != null ? pedido.getTotal() : 0.0
        );

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // Base URI no nula para evitar fallos en resolución interna de recursos.
            builder.withHtmlContent(html, "");
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF de la factura", e);
        }
    }

    private String getLogoUrl() {
        String override = (mailLogoUrl != null) ? mailLogoUrl.trim() : "";
        if (!override.isEmpty()) return override;

        String base = (frontendBaseUrl != null) ? frontendBaseUrl.trim() : "";
        if (base.isEmpty()) return null;
        return base.endsWith("/") ? base + "images/Logo.jpeg" : base + "/images/Logo.jpeg";
    }
}

