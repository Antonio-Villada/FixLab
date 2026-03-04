package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Busca esta parte en tu Pedido.java y asegúrate de que quede así:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_cedula", nullable = false) // Cambiamos a cliente_cedula
    private Usuario cliente;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(nullable = false)
    private Double total;

    // Dirección de envío ingresada en el Checkout
    @Column(length = 255)
    private String direccionEnvio;

    // Relación con los productos comprados
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles;
}