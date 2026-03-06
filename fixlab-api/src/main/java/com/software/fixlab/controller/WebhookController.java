package com.software.fixlab.controller;

import com.software.fixlab.dto.req.WompiTransactionDTO;
import com.software.fixlab.dto.req.WompiWebhookDTO;
import com.software.fixlab.service.interfaces.PedidoService;
import com.software.fixlab.service.interfaces.WompiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WompiService wompiService;
    private final PedidoService pedidoService;

    @PostMapping("/wompi")
    public ResponseEntity<?> recibirNotificacionWompi(@RequestBody WompiWebhookDTO evento) {
        WompiTransactionDTO tx = evento.getData().getTransaction();

        // 1. Validar la firma por seguridad (usando el checksum anidado y el timestamp numérico)
        boolean esValida = wompiService.validarFirmaEvento(
                tx.getId(),
                tx.getStatus(),
                tx.getAmount_in_cents(),
                evento.getTimestamp(),
                evento.getSignature().getChecksum()
        );

        if (!esValida) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firma inválida o corrupta");
        }

        // 2. Si el pago fue aprobado, actualizamos el pedido en nuestra base de datos
        if ("APPROVED".equals(tx.getStatus())) {
            try {
                // Extraemos el ID del pedido de la referencia (Ej: "FIX-15-167890")
                String[] partes = tx.getReference().split("-");
                Integer pedidoId = Integer.parseInt(partes[1]);

                pedidoService.confirmarPago(pedidoId);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // 3. Responder siempre 200 OK a Wompi
        return ResponseEntity.ok().build();
    }
}