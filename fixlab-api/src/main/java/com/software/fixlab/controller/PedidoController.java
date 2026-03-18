package com.software.fixlab.controller;

import com.software.fixlab.dto.req.PedidoReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.PedidoRespDTO;
import com.software.fixlab.dto.resp.WompiCheckoutDTO;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExistePedidoException;
import com.software.fixlab.exception.NoExisteProductoException;
import com.software.fixlab.exception.ResourceNotFoundException;
import com.software.fixlab.service.interfaces.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<?> crearPedido(@RequestBody PedidoReqDTO dto, Authentication authentication) {
        try {
            WompiCheckoutDTO response = pedidoService.crearPedido(dto, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NoExisteProductoException | ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @PostMapping("/{id}/confirmar-pago")
    public ResponseEntity<?> confirmarPagoWompi(@PathVariable Integer id) {
        try {
            String resultado = pedidoService.confirmarPago(id);
            return ResponseEntity.ok(new MensajeRespDTO(resultado));
        } catch (NoExistePedidoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    // --- NUEVAS RUTAS ADMINISTRATIVAS Y DE CLIENTE ---

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PedidoRespDTO>> obtenerTodos() {
        return ResponseEntity.ok(pedidoService.obtenerTodos());
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<List<PedidoRespDTO>> obtenerMisPedidos(Authentication authentication) {
        return ResponseEntity.ok(pedidoService.obtenerMisPedidos(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('ADMIN')")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id, Authentication authentication) {
        try {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (isAdmin) {
                return ResponseEntity.ok(pedidoService.obtenerPorId(id));
            }
            return ResponseEntity.ok(pedidoService.obtenerPorIdParaCliente(id, authentication.getName()));
        } catch (NoExistePedidoException | ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id, @RequestParam String nuevoEstado) {
        try {
            return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado));
        } catch (NoExistePedidoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        }
    }
}