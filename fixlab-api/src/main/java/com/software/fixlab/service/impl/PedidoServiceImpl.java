package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.ItemCarritoDTO;
import com.software.fixlab.dto.req.PedidoReqDTO;
import com.software.fixlab.dto.resp.WompiCheckoutDTO;
import com.software.fixlab.entity.DetallePedido;
import com.software.fixlab.entity.Pedido;
import com.software.fixlab.entity.Producto;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.repository.DetallePedidoRepository;
import com.software.fixlab.repository.PedidoRepository;
import com.software.fixlab.repository.ProductoRepository;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    @Value("${wompi.public-key}")
    private String wompiPublicKey;

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final WompiServiceImpl wompiService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WompiCheckoutDTO crearPedido(PedidoReqDTO dto, String emailUsuario) throws Exception {
        // 1. Buscamos al cliente
        Usuario cliente = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new Exception("Cliente no encontrado"));

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new Exception("El carrito de compras está vacío");
        }

        double totalPedido = 0.0;
        List<DetallePedido> detallesParaGuardar = new ArrayList<>();

        // 2. Creamos el pedido inicial en estado PENDIENTE
        Pedido nuevoPedido = Pedido.builder()
                .cliente(cliente)
                .estado("PENDIENTE")
                .total(0.0)
                .build();

        pedidoRepository.save(nuevoPedido);

        // 3. Procesamos los productos y validamos stock
        for (ItemCarritoDTO item : dto.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new Exception("Producto con ID " + item.getProductoId() + " no encontrado"));

            if (producto.getStock() < item.getCantidad()) {
                throw new Exception("Stock insuficiente para: " + producto.getNombre());
            }

            // Descontamos stock temporalmente (Reserva)
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            double subtotal = item.getCantidad() * producto.getPrecio();
            totalPedido += subtotal;

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(nuevoPedido)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .build();
            detallesParaGuardar.add(detalle);
        }

        // 4. Guardamos detalles y actualizamos el total del pedido
        detallePedidoRepository.saveAll(detallesParaGuardar);
        nuevoPedido.setTotal(totalPedido);
        pedidoRepository.save(nuevoPedido);

        // 5. Preparamos la información para Wompi
        // Wompi requiere el monto en centavos (Ej: 1000.0 -> 100000)
        long montoEnCentavos = (long) (totalPedido * 100);
        String referencia = "FIX-" + nuevoPedido.getId() + "-" + System.currentTimeMillis();

        // Generamos la firma de integridad SHA-256
        String firma = wompiService.generarFirma(referencia, montoEnCentavos, "COP");

        // 6. Retornamos el DTO que Angular usará para abrir el Widget
        return WompiCheckoutDTO.builder()
                .pedidoId(nuevoPedido.getId())
                .referencia(referencia)
                .montoEnCentavos(montoEnCentavos)
                .moneda("COP")
                .firmaIntegridad(firma)
                .llavePublica(wompiPublicKey)
                .build();
    }

    @Override
    @Transactional
    public String confirmarPago(Integer pedidoId) throws Exception {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado"));

        if ("PAGADO".equals(pedido.getEstado())) {
            throw new Exception("Este pedido ya fue pagado anteriormente.");
        }

        // 1. Cambiamos el estado de la base de datos
        pedido.setEstado("PAGADO");
        pedidoRepository.save(pedido);

        // 2. Enviamos la factura legal al correo del cliente
        emailService.enviarFacturaVenta(
                pedido.getCliente().getEmail(),
                pedido.getCliente().getNombre(),
                String.valueOf(pedido.getId()),
                pedido.getTotal()
        );

        return "Pago confirmado exitosamente. Factura enviada al cliente.";
    }
}