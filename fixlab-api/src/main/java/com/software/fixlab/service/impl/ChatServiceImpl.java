package com.software.fixlab.service.impl;

import com.software.fixlab.dto.resp.ChatRespuestaRespDTO;
import com.software.fixlab.dto.resp.PedidoRespDTO;
import com.software.fixlab.service.interfaces.ChatService;
import com.software.fixlab.service.interfaces.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT_FIXLAB =
            "Eres el asistente virtual de FixLab, una tienda/laboratorio. "
                    + "Responde de forma breve y amable. "
                    + "Puedes ayudar con: información de pedidos, productos, horarios y contacto. "
                    + "Si no sabes algo, indica que el usuario puede contactar al equipo de FixLab.";

    private final PedidoService pedidoService;
    private final OpenAIClient openAIClient;

    @Override
    public ChatRespuestaRespDTO responder(String mensajeUsuario, String emailUsuario) {
        String texto = (mensajeUsuario == null ? "" : mensajeUsuario).trim();
        if (texto.isEmpty()) {
            return ChatRespuestaRespDTO.builder()
                    .respuesta("Escribe algo y con gusto te ayudo.")
                    .build();
        }
        String normalizado = texto.toLowerCase(Locale.ROOT);

        // --- Reglas: pedidos / estado / seguimiento ---
        if (contieneAlguno(normalizado, "pedido", "pedidos", "estado", "seguimiento", "último", "ultimo", "compré", "compre", "orden")) {
            return responderPedidos(emailUsuario);
        }

        // --- Reglas: productos / catálogo ---
        if (contieneAlguno(normalizado, "producto", "productos", "catálogo", "catalogo", "precio", "precios", "qué venden", "que venden")) {
            return ChatRespuestaRespDTO.builder()
                    .respuesta("Tenemos un catálogo de productos disponible. Puedes ver todos en la sección Productos. ¿Quieres que te cuente sobre alguna categoría en especial?")
                    .tipoAccion("ver_productos")
                    .build();
        }

        // --- Reglas: contacto / horario / dirección ---
        if (contieneAlguno(normalizado, "contacto", "contactar", "horario", "horarios", "dirección", "direccion", "dónde", "donde", "teléfono", "telefono", "email", "correo")) {
            return ChatRespuestaRespDTO.builder()
                    .respuesta("Puedes contactarnos por correo o revisar nuestra página para horarios y dirección. ¿Necesitas algo más?")
                    .build();
        }

        // --- Saludos ---
        if (contieneAlguno(normalizado, "hola", "buenos días", "buenas tardes", "buenas noches", "hey", "qué tal", "que tal")) {
            return ChatRespuestaRespDTO.builder()
                    .respuesta("¡Hola! Soy el asistente de FixLab. Puedo ayudarte con pedidos, productos o información de contacto. ¿En qué puedo ayudarte?")
                    .build();
        }

        // --- Fallback: IA (OpenAI) si está configurada ---
        Optional<String> respuestaIA = openAIClient.completar(SYSTEM_PROMPT_FIXLAB, texto);
        if (respuestaIA.isPresent()) {
            return ChatRespuestaRespDTO.builder()
                    .respuesta(respuestaIA.get())
                    .build();
        }

        return ChatRespuestaRespDTO.builder()
                .respuesta("¿En qué puedo ayudarte? Puedo informarte sobre tus pedidos, productos o datos de contacto.")
                .build();
    }

    private ChatRespuestaRespDTO responderPedidos(String emailUsuario) {
        try {
            List<PedidoRespDTO> misPedidos = pedidoService.obtenerMisPedidos(emailUsuario);
            if (misPedidos == null || misPedidos.isEmpty()) {
                return ChatRespuestaRespDTO.builder()
                        .respuesta("No tienes pedidos registrados. Cuando hagas una compra podrás consultar su estado aquí.")
                        .build();
            }
            // Último pedido por fecha (más reciente primero)
            PedidoRespDTO ultimo = misPedidos.stream()
                    .max((a, b) -> (a.getFechaCreacion() != null && b.getFechaCreacion() != null)
                            ? a.getFechaCreacion().compareTo(b.getFechaCreacion())
                            : 0)
                    .orElse(misPedidos.get(0));
            String estado = ultimo.getEstado() != null ? ultimo.getEstado() : "sin estado";
            String total = ultimo.getTotal() != null ? String.format("%.2f", ultimo.getTotal()) : "—";
            String respuesta = String.format(
                    "Tu último pedido #%d está en estado: %s. Total: $%s. ¿Quieres ver el detalle?",
                    ultimo.getId(), estado, total);
            return ChatRespuestaRespDTO.builder()
                    .respuesta(respuesta)
                    .tipoAccion("ver_pedido")
                    .payload(String.valueOf(ultimo.getId()))
                    .build();
        } catch (Exception e) {
            log.warn("Error al obtener pedidos para chat: {}", e.getMessage());
            return ChatRespuestaRespDTO.builder()
                    .respuesta("No pude consultar tus pedidos en este momento. Intenta de nuevo o revisa la sección Mis pedidos.")
                    .build();
        }
    }

    private static boolean contieneAlguno(String texto, String... palabras) {
        for (String p : palabras) {
            if (texto.contains(p)) return true;
        }
        return false;
    }
}
