package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.ItemCarritoDTO;
import com.software.fixlab.dto.req.PedidoReqDTO;
import com.software.fixlab.dto.resp.DetallePedidoRespDTO;
import com.software.fixlab.dto.resp.PedidoRespDTO;
import com.software.fixlab.dto.resp.WompiCheckoutDTO;
import com.software.fixlab.entity.DetallePedido;
import com.software.fixlab.entity.Pedido;
import com.software.fixlab.entity.Producto;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExistePedidoException;
import com.software.fixlab.exception.NoExisteProductoException;
import com.software.fixlab.exception.ResourceNotFoundException;
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
import java.util.stream.Collectors;

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
    @Transactional
    public WompiCheckoutDTO crearPedido(PedidoReqDTO dto, String emailUsuario) {
        Usuario cliente = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("El carrito de compras está vacío");
        }

        double totalPedido = 0.0;
        List<DetallePedido> detallesParaGuardar = new ArrayList<>();

        Pedido nuevoPedido = Pedido.builder()
                .cliente(cliente)
                .estado("PENDIENTE")
                .total(0.0)
                .direccionEnvio(dto.getDireccionEnvio()) // <-- Guardamos la dirección
                .build();

        pedidoRepository.save(nuevoPedido);

        for (ItemCarritoDTO item : dto.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new NoExisteProductoException("Producto con ID " + item.getProductoId() + " no encontrado"));

            if (producto.getStock() < item.getCantidad()) {
                throw new BadRequestException("Stock insuficiente para: " + producto.getNombre());
            }

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

        detallePedidoRepository.saveAll(detallesParaGuardar);
        nuevoPedido.setTotal(totalPedido);
        pedidoRepository.save(nuevoPedido);

        long montoEnCentavos = (long) (totalPedido * 100);
        String referencia = "FIX-" + nuevoPedido.getId() + "-" + System.currentTimeMillis();
        String firma;
        try {
            firma = wompiService.generarFirma(referencia, montoEnCentavos, "COP");
        } catch (Exception e) {
            throw new BadRequestException("No se pudo generar la firma de pago: " + e.getMessage());
        }

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
    public String confirmarPago(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NoExistePedidoException("Pedido no encontrado"));

        if ("PAGADO".equals(pedido.getEstado())) {
            throw new BadRequestException("Este pedido ya fue pagado anteriormente.");
        }

        pedido.setEstado("PAGADO");
        pedidoRepository.save(pedido);

        emailService.enviarFacturaVenta(
                pedido.getCliente().getEmail(),
                pedido.getCliente().getNombre(),
                String.valueOf(pedido.getId()),
                pedido.getTotal()
        );

        return "Pago confirmado exitosamente. Factura enviada al cliente.";
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoRespDTO> obtenerTodos() {
        return pedidoRepository.findAll().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoRespDTO> obtenerTodosConFiltros(String estado, Long categoriaId) {
        if (categoriaId != null && categoriaId > 0) {
            List<Integer> pedidoIds = detallePedidoRepository.findDistinctPedidoIdsByProductoCategoriaId(categoriaId);
            if (pedidoIds.isEmpty()) {
                return new ArrayList<>();
            }
            List<Pedido> pedidos = pedidoRepository.findByIdInOrderByFechaCreacionDesc(pedidoIds);
            if (estado != null && !estado.isBlank()) {
                pedidos = pedidos.stream()
                        .filter(p -> estado.equalsIgnoreCase(p.getEstado()))
                        .collect(Collectors.toList());
            }
            return pedidos.stream().map(this::mapearADto).collect(Collectors.toList());
        }
        if (estado != null && !estado.isBlank()) {
            return pedidoRepository.findByEstadoOrderByFechaCreacionDesc(estado).stream()
                    .map(this::mapearADto)
                    .collect(Collectors.toList());
        }
        return obtenerTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoRespDTO> obtenerMisPedidos(String emailUsuario) {
        Usuario cliente = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return pedidoRepository.findByCliente_Cedula(cliente.getCedula()).stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoRespDTO obtenerPorId(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoExistePedidoException("Pedido no encontrado con ID: " + id));
        return mapearADto(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoRespDTO obtenerPorIdParaCliente(Integer id, String emailUsuario) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoExistePedidoException("Pedido no encontrado con ID: " + id));
        if (!pedido.getCliente().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new ResourceNotFoundException("No tiene acceso a este pedido");
        }
        return mapearADto(pedido);
    }

    @Override
    @Transactional
    public PedidoRespDTO actualizarEstado(Integer id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new NoExistePedidoException("Pedido no encontrado con ID: " + id));

        pedido.setEstado(nuevoEstado); // Ej: "ENVIADO", "ENTREGADO", "CANCELADO"
        pedidoRepository.save(pedido);

        return mapearADto(pedido);
    }

    // Método auxiliar para armar la respuesta limpia
    private PedidoRespDTO mapearADto(Pedido pedido) {
        List<DetallePedido> detalles = detallePedidoRepository.findByPedido(pedido);

        List<DetallePedidoRespDTO> detallesDto = detalles.stream().map(d -> DetallePedidoRespDTO.builder()
                .productoId(d.getProducto().getId())
                .nombreProducto(d.getProducto().getNombre())
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .subtotal(d.getCantidad() * d.getPrecioUnitario())
                .build()).collect(Collectors.toList());

        return PedidoRespDTO.builder()
                .id(pedido.getId())
                .fechaCreacion(pedido.getFechaCreacion())
                .total(pedido.getTotal())
                .estado(pedido.getEstado())
                .clienteCedula(pedido.getCliente().getCedula())
                .clienteNombre(pedido.getCliente().getNombre() + " " + pedido.getCliente().getApellido())
                .direccionEnvio(pedido.getDireccionEnvio())
                .detalles(detallesDto)
                .build();
    }
}