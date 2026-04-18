package com.software.fixlab.entity.auditoria;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria_usuario")
public class AuditoriaUsuario extends AuditoriaRegistroBase {}
