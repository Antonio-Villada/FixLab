package com.software.fixlab.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.software.fixlab.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary; // Esto usa el Bean de tu CloudinaryConfig excelente

    @Override
    public String subirImagen(MultipartFile archivo) throws Exception {
        try {
            File archivoTemporal = convertirAFile(archivo);
            Map uploadResult = cloudinary.uploader().upload(archivoTemporal, ObjectUtils.emptyMap());
            archivoTemporal.delete();
            return uploadResult.get("url").toString();
        } catch (Exception e) {
            throw new Exception("Error al subir la imagen: " + e.getMessage());
        }
    }

    @Override
    public String subirEvidencia(MultipartFile archivo) throws Exception {
        try {
            File archivoTemporal = convertirAFile(archivo);
            Map uploadResult = cloudinary.uploader().upload(archivoTemporal,
                    ObjectUtils.asMap("resource_type", "auto"));
            archivoTemporal.delete();
            return uploadResult.get("url").toString();
        } catch (Exception e) {
            throw new Exception("Error al subir la evidencia: " + e.getMessage());
        }
    }

    private File convertirAFile(MultipartFile archivo) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + archivo.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(archivo.getBytes());
        fos.close();
        return convFile;
    }
}