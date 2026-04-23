package com.software.fixlab.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.software.fixlab.util.DisposableEmailValidator;

/**
 * Al arranque de la aplicación, descarga la lista pública de dominios de correo temporal
 * desde GitHub (disposable/disposable-email-domains) y la usa para validar registros.
 * Si la descarga falla (sin red, GitHub caído), se mantiene la lista estática de respaldo.
 */
@Component
public class DisposableEmailDomainsLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DisposableEmailDomainsLoader.class);

    /** Lista mantenida por la comunidad, actualizada frecuentemente */
    private static final String DISPOSABLE_DOMAINS_URL =
            "https://raw.githubusercontent.com/disposable/disposable-email-domains/master/domains.txt";

    @Override
    public void run(ApplicationArguments args) {
        try {
            Set<String> domains = fetchDomainsFromUrl(DISPOSABLE_DOMAINS_URL);
            if (!domains.isEmpty()) {
                DisposableEmailValidator.setDomains(domains);
                log.info("Lista de correos temporales cargada desde GitHub: {} dominios.", domains.size());
            } else {
                log.warn("La URL devolvió 0 dominios; se mantiene la lista de respaldo.");
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar la lista de correos temporales desde GitHub ({}). Se usa la lista de respaldo. Error: {}",
                    DISPOSABLE_DOMAINS_URL, e.getMessage());
        }
    }

    private Set<String> fetchDomainsFromUrl(String urlString) throws Exception {
        try (var reader = new BufferedReader(
                new InputStreamReader(URI.create(urlString).toURL().openStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toSet());
        }
    }
}
