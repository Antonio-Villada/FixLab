package com.software.fixlab.controller;

import com.software.fixlab.dto.req.WompiTransactionDTO;
import com.software.fixlab.dto.req.WompiWebhookDTO;
import com.software.fixlab.service.impl.WompiServiceImpl;
import com.software.fixlab.service.interfaces.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WompiServiceImpl wompiService;
    private final PedidoService pedidoService;

    @PostMapping("/wompi")
    public ResponseEntity<?> recibirNotificacionWompi(@RequestBody WompiWebhookDTO evento) {
        WompiTransactionDTO tx = evento.getData().getTransaction();

        // 1. Validar la firma por seguridad
        boolean esValida = wompiService.validarFirmaEvento(
                tx.getId(),
                tx.getStatus(),
                tx.getAmount_in_cents(),
                evento.getSent_at(),
                evento.getSignature()
        );

        if (!esValida) {
            return ResponseEntity.status(401).body("Firma inválida");
        }

        // 2. Si el pago fue aprobado, actualizamos el pedido
        if ("APPROVED".equals(tx.getStatus())) {
            try {
                // Extraemos el ID del pedido de la referencia (Ej: "FIX-15-...")
                String[] partes = tx.getReference().split("-");
                Integer pedidoId = Integer.parseInt(partes[1]);

                pedidoService.confirmarPago(pedidoId);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }

        // Wompi espera siempre un 200 OK
        return ResponseEntity.ok().build();
    }
}