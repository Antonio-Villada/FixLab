package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.resp.ExistenciasReporteRespDTO;

public interface ReporteExistenciasService {

    ExistenciasReporteRespDTO generarReporte();

    byte[] generarReporteCsvUtf8();
}
