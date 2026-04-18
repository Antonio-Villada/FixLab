package com.software.fixlab.entity.auditoria;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria_pedido")
public class AuditoriaPedido extends AuditoriaRegistroBase {}
