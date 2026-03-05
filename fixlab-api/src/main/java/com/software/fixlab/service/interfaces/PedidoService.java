package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.PedidoReqDTO;
import com.software.fixlab.dto.resp.WompiCheckoutDTO;

public interface PedidoService {
    // Retornará el ID del pedido creado para que Angular se lo mande a Wompi
    WompiCheckoutDTO crearPedido(PedidoReqDTO dto, String emailUsuario) throws Exception;
    // Este método se ejecutará cuando Wompi diga "Pago Exitoso"
    String confirmarPago(Integer pedidoId) throws Exception;


}