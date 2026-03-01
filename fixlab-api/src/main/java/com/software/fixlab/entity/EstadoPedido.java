package com.software.fixlab.entity;

public enum EstadoPedido {
    CARRITO,         // Paso 1 y 2: Búsqueda y Gestión de carrito
    PROCESANDO_PAGO, // Paso 3: Checkout (Max 3 pasos lógicos)
    PAGADO,          // Paso 4: Verificación y Facturación
    EN_PREPARACION,  // Paso 5: Picking en bodega
    DESPACHADO,      // Paso 6: Entregado a transportadora
    ENTREGADO,       // Cliente recibe
    DEVUELTO         // Integración con Garantías/Devoluciones
}