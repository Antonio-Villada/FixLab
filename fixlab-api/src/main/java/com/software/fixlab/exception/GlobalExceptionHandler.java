package com.software.fixlab.exception;

import com.software.fixlab.dto.resp.MensajeRespDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MensajeRespDTO> manejarBadRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeRespDTO(e.getMessage()));
    }

    /**
     * Sin handler dedicado, el {@link #manejarExcepcionesGenerales(Exception)} devolvía 400 con el mensaje
     * confuso "No static resource …" de Spring cuando la URL no coincide con ningún controlador.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<MensajeRespDTO> manejarRutaSinControlador(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeRespDTO("Ruta no disponible en el API: " + e.getResourcePath()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MensajeRespDTO> manejarAccesoDenegado(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MensajeRespDTO("No autorizado para esta operación"));
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            NoExistePedidoException.class,
            NoExisteProductoException.class,
            NoExisteCategoriaException.class,
            NoExisteTipoProductoException.class,
            NoExisteReparacionException.class,
            NoExisteEquipoException.class,
            NoExisteTallerException.class,
            NoExisteSolicitudPqrException.class
    })
    public ResponseEntity<MensajeRespDTO> manejarNoEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MensajeRespDTO(e.getMessage()));
    }

    // Atrapa el resto de errores de lógica de negocio (p. ej. Exception en AuthServiceImpl).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeRespDTO> manejarExcepcionesGenerales(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MensajeRespDTO(e.getMessage()));
    }

    // Atrapa los errores de validación de los DTOs (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarExcepcionesDeValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}