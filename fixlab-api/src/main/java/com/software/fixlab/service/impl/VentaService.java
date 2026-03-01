package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.CheckoutReqDTO;
import com.software.fixlab.dto.req.ItemCarritoDTO;
import com.software.fixlab.entity.*;
import com.software.fixlab.repository.PedidoRepository;
import com.software.fixlab.repository.ProductoRepository;
import com.software.fixlab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Pedido procesarCheckout(String emailCliente, CheckoutReqDTO checkoutDTO) throws Exception {

        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new Exception("Cliente no encontrado"));

        if (checkoutDTO.getItems() == null || checkoutDTO.getItems().isEmpty()) {
            throw new Exception("El carrito no puede estar vacío");
        }

        double totalPedido = 0.0;
        List<DetallePedido> detalles = new ArrayList<>();

        // Creamos la cabecera del pedido (aún sin guardar)
        Pedido nuevoPedido = Pedido.builder()
                .cliente(cliente)
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.PROCESANDO_PAGO) // Estado inicial según tu Enum
                .direccionEnvio(checkoutDTO.getDireccionEnvio())
                .build();

        // Evaluamos cada producto del carrito
        for (ItemCarritoDTO item : checkoutDTO.getItems()) {

            // Regla de Negocio: Máximo 5 unidades de la misma referencia
            if (item.getCantidad() > 5) {
                throw new Exception("No puedes llevar más de 5 unidades del mismo producto.");
            }

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new Exception("Producto no encontrado con ID: " + item.getProductoId()));

            // Regla de Calidad: Verificar stock disponible
            if (producto.getStock() < item.getCantidad()) {
                throw new Exception("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Calculamos subtotal seguro con el precio de la base de datos
            double subtotal = producto.getPrecio() * item.getCantidad();
            totalPedido += subtotal;

            // Preparamos el detalle del pedido
            DetallePedido detalle = DetallePedido.builder()
                    .pedido(nuevoPedido)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();

            detalles.add(detalle);
        }

        // Asignamos los detalles y el total calculado al pedido
        nuevoPedido.setDetalles(detalles);
        nuevoPedido.setTotal(totalPedido);

        // Guardamos todo en cascada (Pedido + Detalles) gracias al CascadeType.ALL que configuramos
        return pedidoRepository.save(nuevoPedido);
    }
}