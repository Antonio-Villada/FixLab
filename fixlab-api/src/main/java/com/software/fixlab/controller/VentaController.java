package com.software.fixlab.controller;

import com.software.fixlab.dto.req.CheckoutReqDTO;
import com.software.fixlab.dto.resp.CheckoutRespDTO;
import com.software.fixlab.entity.Pedido;
import com.software.fixlab.service.impl.PagoService;
import com.software.fixlab.service.impl.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final PagoService pagoService; // <-- Inyectamos el servicio de pagos

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<CheckoutRespDTO> procesarCheckout(
            @RequestBody CheckoutReqDTO checkoutDTO,
            Authentication authentication) throws Exception {

        String emailCliente = authentication.getName();

        // 1. Guardamos el pedido en PostgreSQL validando stock e inventario
        Pedido nuevoPedido = ventaService.procesarCheckout(emailCliente, checkoutDTO);

        // 2. Nos comunicamos con Mercado Pago para generar el link seguro
        String urlPago = pagoService.crearPreferenciaPago(nuevoPedido);

        // 3. Devolvemos el link al cliente
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CheckoutRespDTO(nuevoPedido.getId(), urlPago));
    }
    @GetMapping("/pago-exitoso")
    public ResponseEntity<String> pagoExitoso(
            @RequestParam(name = "collection_status") String status,
            @RequestParam(name = "external_reference") String externalReference) {

        try {
            // Llamamos a nuestro servicio para actualizar todo
            String mensaje = ventaService.confirmarPago(externalReference, status);
            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al procesar el pago: " + e.getMessage());
        }
    }
}