package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.PedidoReqDTO;
import com.software.fixlab.dto.resp.PedidoRespDTO;
import com.software.fixlab.dto.resp.WompiCheckoutDTO;

import java.util.List;

public interface PedidoService {
    WompiCheckoutDTO crearPedido(PedidoReqDTO dto, String emailUsuario);
    String confirmarPago(Integer pedidoId);

    // Nuevos métodos del CRUD
    List<PedidoRespDTO> obtenerTodos();
    List<PedidoRespDTO> obtenerMisPedidos(String emailUsuario);
    PedidoRespDTO obtenerPorId(Integer id);
    PedidoRespDTO actualizarEstado(Integer id, String nuevoEstado);
}