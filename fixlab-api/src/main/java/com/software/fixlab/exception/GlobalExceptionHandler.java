package com.software.fixlab.exception;

import com.software.fixlab.dto.resp.MensajeRespDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Atrapa los errores de lógica de negocio (ej. "El correo ya existe", "Cuenta bloqueada")
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