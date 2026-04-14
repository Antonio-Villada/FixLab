package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.SolicitudPqrCambiarEstadoReqDTO;
import com.software.fixlab.dto.req.SolicitudPqrCreateReqDTO;
import com.software.fixlab.dto.req.SolicitudPqrValidacionGarantiaReqDTO;
import com.software.fixlab.dto.resp.SolicitudPqrRespDTO;
import com.software.fixlab.entity.RolUsuario;

import java.util.List;

public interface SolicitudPqrService {

    SolicitudPqrRespDTO crear(SolicitudPqrCreateReqDTO dto, String emailUsuario);

    List<SolicitudPqrRespDTO> listarMis(String emailUsuario);

    List<SolicitudPqrRespDTO> listarGestion(String emailUsuario, RolUsuario rol);

    SolicitudPqrRespDTO obtenerPorId(Long id, String emailUsuario, RolUsuario rol);

    SolicitudPqrRespDTO cambiarEstado(Long id, SolicitudPqrCambiarEstadoReqDTO dto, String emailUsuario, RolUsuario rol);

    SolicitudPqrRespDTO registrarValidacionGarantiaFisica(
            Long id, SolicitudPqrValidacionGarantiaReqDTO dto, String emailUsuario, RolUsuario rol);
}
