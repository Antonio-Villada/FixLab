package com.software.fixlab.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String subirImagen(MultipartFile archivo) throws Exception;

    /** Imagen o video (resource_type auto) para evidencias de PQRS. */
    String subirEvidencia(MultipartFile archivo) throws Exception;
}