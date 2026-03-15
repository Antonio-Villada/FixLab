package com.software.fixlab.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Valida que un correo electrónico no pertenezca a un proveedor de correo temporal/desechable.
 * La lista de dominios se carga al arranque desde
 * <a href="https://github.com/disposable/disposable-email-domains">GitHub (disposable-email-domains)</a>.
 * Si la descarga falla, se usa una lista estática de respaldo.
 */
public final class DisposableEmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@([^@]+)$");

    /** Lista de respaldo cuando no se puede cargar desde la URL */
    private static final Set<String> FALLBACK_DOMAINS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "10minutemail.com", "10minutemail.net", "guerrillamail.com", "guerrillamail.net",
            "mailinator.com", "maildrop.cc", "mohmal.com", "temp-mail.org", "temp-mail.com",
            "tempmail.com", "tempmail.net", "dispostable.com", "throwaway.io", "yopmail.com",
            "3dkai.com", "getnada.com", "mailnesia.com", "sharklasers.com", "trashmail.com"
    )));

    /** Lista activa: al inicio es la de respaldo; el loader puede reemplazarla por la de GitHub */
    private static volatile Set<String> disposableDomains = FALLBACK_DOMAINS;

    private DisposableEmailValidator() {
    }

    /**
     * Reemplaza la lista de dominios desechables (llamado por {@link com.software.fixlab.config.DisposableEmailDomainsLoader}).
     */
    public static void setDomains(Set<String> domains) {
        disposableDomains = domains == null || domains.isEmpty()
                ? FALLBACK_DOMAINS
                : Collections.unmodifiableSet(new HashSet<>(domains));
    }

    /**
     * Verifica si el correo pertenece a un dominio temporal/desechable.
     *
     * @param email correo electrónico a validar (ej: usuario&#64;10minutemail.com)
     * @return true si es un correo temporal (debe rechazarse), false si es válido
     */
    public static boolean isDisposable(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        var matcher = EMAIL_PATTERN.matcher(email.trim().toLowerCase());
        if (!matcher.matches()) {
            return false;
        }
        String domain = matcher.group(1).toLowerCase();
        return disposableDomains.contains(domain);
    }
}
