package com.software.fixlab.controller;

import com.software.fixlab.dto.req.PedidoReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.WompiCheckoutDTO;
import com.software.fixlab.service.interfaces.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * PASO 1: Crear el pedido y generar firma de Wompi.
     * El Frontend (Angular) llama a este endpoint antes de abrir el widget de pagos.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<?> crearPedido(@RequestBody PedidoReqDTO dto, Authentication authentication) {
        try {
            // Obtenemos el email directamente del token de seguridad
            String emailUsuarioLogueado = authentication.getName();

            // Llamamos al servicio que ahora devuelve todo lo necesario para Wompi
            WompiCheckoutDTO response = pedidoService.crearPedido(dto, emailUsuarioLogueado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    /**
     * PASO 2: Confirmar el pago.
     * Este endpoint se activa cuando recibimos la notificación de éxito de la pasarela.
     */
    @PostMapping("/{id}/confirmar-pago")
    public ResponseEntity<?> confirmarPagoWompi(@PathVariable Integer id) {
        try {
            String resultado = pedidoService.confirmarPago(id);
            return ResponseEntity.ok(new MensajeRespDTO(resultado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }
}