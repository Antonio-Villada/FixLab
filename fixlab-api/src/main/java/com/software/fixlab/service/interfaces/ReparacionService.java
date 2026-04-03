package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.*;
import com.software.fixlab.entity.RolUsuario;

import java.util.List;

public interface ReparacionService {

    List<TipoEquipoRespDTO> listarTiposEquipo();

    List<TipoTallerRespDTO> listarTiposTaller();

    List<TallerRespDTO> listarTalleres();

    ReparacionRespDTO crear(ReparacionCreateReqDTO dto, String emailUsuario, RolUsuario rol);

    List<ReparacionRespDTO> listar(String emailUsuario, RolUsuario rol);

    ReparacionRespDTO obtenerPorId(Integer id, String emailUsuario, RolUsuario rol);

    ReparacionRespDTO obtenerPorNumeroTicket(String numeroTicket, String emailUsuario, RolUsuario rol);

    ReparacionRespDTO asignarTecnico(Integer id, ReparacionAsignarTecnicoReqDTO dto, String emailUsuario, RolUsuario rol);

    ReparacionRespDTO registrarDiagnosticoCotizacion(Integer id, ReparacionDiagnosticoCotizacionReqDTO dto,
                                                     String emailUsuario, RolUsuario rol);

    ReparacionRespDTO cambiarEstado(Integer id, ReparacionCambiarEstadoReqDTO dto, String emailUsuario, RolUsuario rol);

    ReparacionRespDTO agregarProducto(Integer id, ReparacionProductoReqDTO dto, String emailUsuario, RolUsuario rol);

    ReparacionRespDTO agregarEvidencia(Integer id, ReparacionEvidenciaReqDTO dto, String emailUsuario, RolUsuario rol);

    ReparacionRespDTO aprobarCotizacion(Integer id, String emailUsuario, RolUsuario rol);
}
