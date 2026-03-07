package com.software.fixlab.controller;

import com.software.fixlab.dto.req.WompiTransactionDTO;
import com.software.fixlab.dto.req.WompiWebhookDTO;
import com.software.fixlab.service.interfaces.PedidoService;
import com.software.fixlab.service.interfaces.WompiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WompiService wompiService;
    private final PedidoService pedidoService;

    @PostMapping("/wompi")
    public ResponseEntity<?> recibirNotificacionWompi(@RequestBody WompiWebhookDTO evento) {
        log.info("Webhook Wompi recibido: event={}", evento != null ? evento.getEvent() : null);
        if (evento == null || evento.getData() == null || evento.getData().getTransaction() == null) {
            return ResponseEntity.badRequest().build();
        }
        WompiTransactionDTO tx = evento.getData().getTransaction();

        // 1. Validar la firma (properties + timestamp + secreto, ver docs Wompi Colombia)
        if (!wompiService.validarFirmaEvento(evento)) {
            log.warn("Webhook Wompi rechazado: firma inválida");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firma inválida o corrupta");
        }

        // 2. Si el pago fue aprobado, actualizamos el pedido
        if ("APPROVED".equals(tx.getStatus()) && tx.getReference() != null) {
            try {
                String[] partes = tx.getReference().split("-");
                if (partes.length >= 2) {
                    Integer pedidoId = Integer.parseInt(partes[1]);
                    pedidoService.confirmarPago(pedidoId);
                    log.info("Pedido {} marcado como PAGADO (webhook Wompi)", pedidoId);
                }
            } catch (Exception e) {
                log.error("Error al confirmar pago desde webhook: reference={}", tx.getReference(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok().build();
    }
}