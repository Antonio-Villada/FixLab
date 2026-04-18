package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.EliminarCuentaClienteReqDTO;
import com.software.fixlab.dto.req.UsuarioUpdateReqDTO;
import com.software.fixlab.dto.resp.ClienteSugerenciaRespDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.StaffTallerAsignableRespDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import com.software.fixlab.entity.EstadoReparacion;
import com.software.fixlab.entity.EstadoSolicitudPqr;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.ResourceNotFoundException;
import com.software.fixlab.mapper.UsuarioMapper;
import com.software.fixlab.repository.PedidoRepository;
import com.software.fixlab.repository.ReparacionRepository;
import com.software.fixlab.repository.SolicitudPqrRepository;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.CloudinaryService;
import com.software.fixlab.service.interfaces.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final Set<EstadoReparacion> ESTADOS_REPARACION_TERMINALES =
            Set.of(EstadoReparacion.ENTREGADO, EstadoReparacion.CANCELADO);

    private static final List<String> ESTADOS_PEDIDO_TERMINALES = List.of("ENTREGADO", "CANCELADO");

    private static final Set<EstadoSolicitudPqr> ESTADOS_PQR_TERMINALES =
            Set.of(EstadoSolicitudPqr.RESUELTO, EstadoSolicitudPqr.CERRADO);

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final CloudinaryService cloudinaryService;
    private final ReparacionRepository reparacionRepository;
    private final PedidoRepository pedidoRepository;
    private final SolicitudPqrRepository solicitudPqrRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRespDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteSugerenciaRespDTO> buscarSugerenciasClientesPorCedula(String fragmento) {
        if (fragmento == null) {
            return List.of();
        }
        String q = fragmento.trim();
        if (q.length() < 2) {
            return List.of();
        }
        return usuarioRepository
                .findTop20ByRolAndCedulaContainingIgnoreCaseAndActivoTrueOrderByCedulaAsc(RolUsuario.CLIENTE, q)
                .stream()
                .map(u -> ClienteSugerenciaRespDTO.builder()
                        .cedula(u.getCedula())
                        .nombre(u.getNombre())
                        .apellido(u.getApellido())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffTallerAsignableRespDTO> listarStaffAsignableComoTecnico() {
        return usuarioRepository
                .findByRolInOrderByApellidoAscNombreAsc(EnumSet.of(RolUsuario.TECNICO, RolUsuario.ADMIN))
                .stream()
                .map(u -> StaffTallerAsignableRespDTO.builder()
                        .cedula(u.getCedula())
                        .nombre(u.getNombre())
                        .apellido(u.getApellido())
                        .rol(u.getRol())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRespDTO obtenerPorCedula(String cedula) {
        Usuario usuario = usuarioRepository.findById(cedula)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con cédula: " + cedula));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRespDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional
    public UsuarioRespDTO actualizarUsuario(String cedula, UsuarioUpdateReqDTO dto) {
        Usuario usuario = usuarioRepository.findById(cedula)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con cédula: " + cedula));

        // Actualizamos solo los campos permitidos
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setTelefono(dto.getTelefono());
        if (dto.getFotoUrl() != null) {
            usuario.setFotoUrl(dto.getFotoUrl());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuarioActualizado);
    }

    @Override
    @Transactional
    public UsuarioRespDTO actualizarMiPerfil(String email, UsuarioUpdateReqDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        if (dto.getNombre() != null) usuario.setNombre(dto.getNombre());
        if (dto.getApellido() != null) usuario.setApellido(dto.getApellido());
        if (dto.getTelefono() != null) usuario.setTelefono(dto.getTelefono());
        if (dto.getFotoUrl() != null) usuario.setFotoUrl(dto.getFotoUrl());

        Usuario actualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(actualizado);
    }

    @Override
    @Transactional
    public UsuarioRespDTO subirFotoPerfil(String email, MultipartFile foto) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar una imagen");
        }
        String urlFoto = cloudinaryService.subirImagen(foto);
        usuario.setFotoUrl(urlFoto);
        usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(String cedula) {
        Usuario usuario = usuarioRepository.findById(cedula)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con cédula: " + cedula));

        usuarioRepository.delete(usuario);
    }

    @Override
    @Transactional
    public MensajeRespDTO eliminarMiCuentaCliente(String email, EliminarCuentaClienteReqDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new BadRequestException("Solo los clientes pueden eliminar su cuenta desde esta opción.");
        }

        if (dto.getPassword() == null || !passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new BadRequestException("La contraseña no es correcta.");
        }

        String cedula = usuario.getCedula();

        if (reparacionRepository.existsByCliente_CedulaAndEstadoNotIn(cedula, ESTADOS_REPARACION_TERMINALES)) {
            throw new BadRequestException(
                    "No puedes eliminar tu cuenta mientras tengas una reparación en curso. "
                            + "Cuando el servicio esté finalizado o cancelado podrás solicitarlo de nuevo.");
        }

        if (pedidoRepository.existsByCliente_CedulaAndEstadoNotIn(cedula, ESTADOS_PEDIDO_TERMINALES)) {
            throw new BadRequestException(
                    "No puedes eliminar tu cuenta mientras tengas un pedido en curso (por ejemplo pendiente de pago o en envío). "
                            + "Espera a que el pedido quede entregado o cancelado.");
        }

        if (solicitudPqrRepository.existsByCliente_CedulaAndEstadoNotIn(cedula, ESTADOS_PQR_TERMINALES)) {
            throw new BadRequestException(
                    "No puedes eliminar tu cuenta mientras tengas una solicitud PQRS abierta. "
                            + "Cuando esté resuelta o cerrada podrás eliminar tu cuenta.");
        }

        usuario.setActivo(false);
        usuario.setCodigoLogin2fa(null);
        usuario.setExpiracionCodigoLogin2fa(null);
        usuario.setUltimoEnvioCodigoLogin2fa(null);
        usuario.setIntentosCodigoLogin2fa(0);
        usuario.setCodigoVerificacion(null);
        usuario.setExpiracionCodigo(null);
        usuario.setTokenRecuperacion(null);
        usuario.setExpiracionToken(null);
        usuario.setCodigoRecuperacion(null);
        usuario.setExpiracionCodigoRecuperacion(null);

        usuarioRepository.save(usuario);

        return new MensajeRespDTO(
                "Tu cuenta ha sido eliminada por completo de nuestro sistema. Gracias por haber usado FixLab.");
    }
}