package com.software.fixlab.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Misma lógica híbrida que el frontend (reglas + respuesta genérica).
 */
public final class ChatHybridResponder {

    private ChatHybridResponder() {}

    public static String reply(String userText) {
        if (userText == null || userText.isBlank()) {
            return "Escribe una pregunta breve.";
        }
        String n = normalize(userText);

        if (Pattern.compile("\\b(hola|buenas|hey|hi)\\b", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "¡Hola! Pregúntame por pedidos, pagos (Wompi), envíos, productos o cuenta y contraseña.";
        }
        if (Pattern.compile("\\b(gracias|thank)\\b", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "¡Con gusto! Si necesitas algo más, aquí estaré.";
        }
        if (Pattern.compile("(pedido|orden|compra realizada|estado del pedido|mis compras)", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "Para ver tus pedidos, inicia sesión y entra al panel (dashboard). Los administradores pueden gestionar pedidos desde Admin → Pedidos.";
        }
        if (Pattern.compile("(pago|pagar|wompi|tarjeta|checkout|transaccion)", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "Los pagos se procesan con Wompi. Añade productos al carrito, revisa el total y completa el pago en la pasarela. Si algo falla, revisa el correo o intenta de nuevo.";
        }
        if (Pattern.compile("(envio|envío|domicilio|entrega|recibir)", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "Los tiempos y costos de envío dependen de tu zona y del pedido. Tras pagar, podrás ver el estado del pedido en tu cuenta.";
        }
        if (Pattern.compile("(producto|catalogo|catálogo|comprar|tienda)", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "Explora el catálogo en Productos desde el menú. Ahí puedes filtrar y añadir al carrito.";
        }
        if (Pattern.compile("(cuenta|registro|registrarme|contraseña|password|clave|correo verificado)", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "Puedes registrarte o iniciar sesión desde el menú. Si olvidaste la contraseña, usa Recuperar contraseña. El inicio de sesión puede pedirte un código por correo.";
        }
        if (Pattern.compile("(devolucion|devolución|reembolso)", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "Para devoluciones o cambios, contacta al equipo de FixLab con tu número de pedido. Un administrador podrá orientarte según la política de la tienda.";
        }
        if (Pattern.compile("(horario|contacto|soporte|ayuda humana|hablar con)", Pattern.CASE_INSENSITIVE).matcher(n).find()) {
            return "Este asistente responde dudas frecuentes. Para un caso concreto, escribe al soporte de FixLab o revisa la sección de contacto del sitio.";
        }
        return "No tengo una respuesta exacta para eso. Prueba con palabras como pedido, pago, envío, productos o contraseña. "
                + "También puedes usar el menú para navegar la tienda.";
    }

    private static String normalize(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }
}
