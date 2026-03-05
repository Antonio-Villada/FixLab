package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    private Integer id;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false, length = 20)
    private String estado; // Ej: PENDIENTE, PAGADO, ENVIADO

    // Relación con el Cliente (Usando tu nueva llave primaria: Cédula)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_cedula", nullable = false)
    private Usuario cliente;
}