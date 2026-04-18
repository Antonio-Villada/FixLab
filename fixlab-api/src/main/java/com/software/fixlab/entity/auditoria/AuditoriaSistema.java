package com.software.fixlab.entity.auditoria;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Auditoría de catálogos y secundarios (categoría, tipos de producto/equipo, chat, webhooks, pruebas)
 * y cualquier controlador no mapeado a una tabla de dominio propia.
 */
@Entity
@Table(name = "auditoria_sistema")
public class AuditoriaSistema extends AuditoriaRegistroBase {}
