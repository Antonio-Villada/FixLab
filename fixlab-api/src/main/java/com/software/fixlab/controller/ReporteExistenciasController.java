package com.software.fixlab.controller;

import com.software.fixlab.dto.resp.ExistenciasReporteRespDTO;
import com.software.fixlab.service.interfaces.ReporteExistenciasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reportes/existencias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteExistenciasController {

    private final ReporteExistenciasService reporteExistenciasService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ExistenciasReporteRespDTO obtenerJson() {
        return reporteExistenciasService.generarReporte();
    }

    @GetMapping(value = "/csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> descargarCsv() {
        byte[] bytes = reporteExistenciasService.generarReporteCsvUtf8();
        String nombre = "existencias_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }
}
