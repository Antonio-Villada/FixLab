package com.software.fixlab.service;

import com.software.fixlab.entity.auditoria.*;
import com.software.fixlab.repository.auditoria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Enruta los registros de auditoría HTTP hacia la tabla del dominio correspondiente.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaPersistenceService {

    private final AuditoriaProductoRepository auditoriaProductoRepository;
    private final AuditoriaPedidoRepository auditoriaPedidoRepository;
    private final AuditoriaReparacionRepository auditoriaReparacionRepository;
    private final AuditoriaUsuarioRepository auditoriaUsuarioRepository;
    private final AuditoriaSolicitudPqrRepository auditoriaSolicitudPqrRepository;
    private final AuditoriaEquipoRepository auditoriaEquipoRepository;
    private final AuditoriaAuthRepository auditoriaAuthRepository;
    private final AuditoriaSistemaRepository auditoriaSistemaRepository;

    /**
     * Persiste en la tabla de auditoría del módulo indicado (mismo criterio que deduce {@code AuditoriaAspect}).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardar(String moduloDeducido, String usuarioEmail, String accion, String detalle, LocalDateTime fechaHora) {
        String modulo = moduloDeducido != null ? moduloDeducido : "DESCONOCIDO";
        switch (modulo) {
            case "PRODUCTO" -> save(new AuditoriaProducto(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaProductoRepository::save);
            case "PEDIDO" -> save(new AuditoriaPedido(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaPedidoRepository::save);
            case "REPARACION" -> save(new AuditoriaReparacion(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaReparacionRepository::save);
            case "USUARIO" -> save(new AuditoriaUsuario(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaUsuarioRepository::save);
            case "SOLICITUDPQR" -> save(new AuditoriaSolicitudPqr(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaSolicitudPqrRepository::save);
            case "EQUIPO" -> save(new AuditoriaEquipo(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaEquipoRepository::save);
            case "AUTH" -> save(new AuditoriaAuth(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaAuthRepository::save);
            default -> save(new AuditoriaSistema(), modulo, usuarioEmail, accion, detalle, fechaHora, auditoriaSistemaRepository::save);
        }
    }

    private <T extends AuditoriaRegistroBase> void save(
            T row,
            String modulo,
            String usuarioEmail,
            String accion,
            String detalle,
            LocalDateTime fechaHora,
            java.util.function.Consumer<T> saver) {
        row.setUsuarioEmail(usuarioEmail);
        row.setModulo(modulo);
        row.setAccion(accion);
        row.setDetalle(detalle);
        row.setFechaHora(fechaHora);
        saver.accept(row);
    }
}
