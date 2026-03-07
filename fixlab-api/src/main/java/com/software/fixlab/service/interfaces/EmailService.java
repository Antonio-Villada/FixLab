package com.software.fixlab.service.interfaces;

public interface EmailService {
    // Para el Proceso 1 (Registro)
    void enviarCodigoVerificacion(String email, String nombre, String codigo);

    // Para el Proceso 2 (Ventas)
    void enviarFacturaVenta(String email, String nombre, String pedidoId, Double total);
}