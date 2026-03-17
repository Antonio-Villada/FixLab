# Guía paso a paso: Implementar chatbot en FixLab

Tu proyecto tiene **backend** (fixlab-api, Spring Boot 4 + Java 21) y **frontend** (fixlab-web, Angular 21). Esta guía te lleva desde cero hasta un chatbot funcional.

---

## 1. Decidir el tipo de chatbot

Elige según lo que necesites:

| Opción | Descripción | Complejidad | Uso típico |
|--------|-------------|-------------|------------|
| **A) Reglas / FAQ** | Respuestas por palabras clave o preguntas frecuentes | Baja | Horarios, políticas, estado de pedido |
| **B) IA externa** | Integrar OpenAI, Claude, etc. | Media | Respuestas libres, más naturales |
| **C) Híbrido** | Reglas para pedidos/productos + IA para el resto | Media-Alta | Mejor experiencia sin depender solo de IA |

**Recomendación para FixLab:** empezar con **A** o **C** (reglas para “mi pedido”, “productos”, “contacto” y luego añadir IA si quieres).

---

## 2. Arquitectura general

```
[Angular fixlab-web]  -->  POST /api/chat/mensaje  -->  [Spring Boot fixlab-api]
        |                                                    |
        |                                                    v
        |                                            [ChatService]
        |                                                    |
        |                                    +---------------+---------------+
        |                                    |               |               |
        |                              Reglas/FAQ      OpenAI/Claude   (opcional)
        |                                    |               |               |
        v                                    v               v               v
  Muestra respuesta  <--  JSON { mensaje }  <--  Respuesta generada
```

- El frontend envía el mensaje del usuario.
- El backend lo procesa (reglas, IA o ambos) y devuelve un texto (y opcionalmente acciones como “ver pedido”).
- El frontend muestra la respuesta en la UI del chat.

---

## 3. Paso a paso en el BACKEND (fixlab-api)

### Paso 3.1 – DTOs de request/response

Crear en `fixlab-api`:

**Request:** `dto/req/ChatMensajeReqDTO.java`

```java
package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMensajeReqDTO {
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 2000)
    private String mensaje;
    
    // Opcional: para historial por usuario
    // private String sessionId;
}
```

**Response:** `dto/resp/ChatRespuestaRespDTO.java`

```java
package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRespuestaRespDTO {
    private String respuesta;
    // Opcional: para botones o acciones (ej: "ver_pedido", "ver_productos")
    private String tipoAccion;
    private String payload; // ej: id de pedido
}
```

### Paso 3.2 – Servicio de chat (versión con reglas/FAQ)

Crear interfaz: `service/interfaces/ChatService.java`

```java
package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.resp.ChatRespuestaRespDTO;

public interface ChatService {
    ChatRespuestaRespDTO responder(String mensajeUsuario, Long usuarioId);
}
```

Crear implementación: `service/impl/ChatServiceImpl.java`

- Recibe `mensajeUsuario` y opcionalmente `usuarioId` (para personalizar “tu pedido”, etc.).
- Normalizar mensaje: `toLowerCase()`, trim.
- Si contiene “pedido”, “seguimiento”, “estado” → llamar a `PedidoService` (ej. último pedido del usuario) y armar respuesta.
- Si contiene “producto”, “catálogo”, “precio” → respuesta genérica o listado resumido.
- Si contiene “contacto”, “horario”, “dirección” → respuesta fija de contacto/horarios.
- Si no coincide con nada → respuesta por defecto: “¿En qué puedo ayudarte? Puedo informarte sobre pedidos, productos o contacto.”
- Devolver `ChatRespuestaRespDTO` con `respuesta`, y opcionalmente `tipoAccion` y `payload`.

Aquí puedes reutilizar tus servicios existentes (`PedidoService`, `ProductoService`, etc.).

### Paso 3.3 – Controlador REST

Crear: `controller/ChatController.java`

- `@RestController`
- `@RequestMapping("/api/chat")`
- Un método: `POST /mensaje` que:
  - Recibe `@RequestBody @Valid ChatMensajeReqDTO`
  - Obtiene el usuario autenticado (con `JwtFilter` ya tienes el usuario en el contexto; usa `SecurityContextHolder` o inyectar un servicio que resuelva el `Usuario` desde el JWT).
  - Llama a `ChatService.responder(dto.getMensaje(), usuario.getId())`
  - Devuelve `ChatRespuestaRespDTO` (status 200).

Solo usuarios autenticados deben poder enviar mensajes; no exponer `/api/chat/**` como `permitAll()` si quieres que el chat sea por usuario.

### Paso 3.4 – Seguridad

En `SecurityConfig`:

- No agregar `"/api/chat/**"` a `permitAll()`.
- Dejar que `anyRequest().authenticated()` exija JWT para `/api/chat/**`.

Así el chatbot solo lo usa quien esté logueado (o puedes crear un endpoint público distinto si quieres chat anónimo).

---

## 4. Paso a paso en el FRONTEND (fixlab-web)

### Paso 4.1 – Servicio Angular para el chat

Crear un servicio, por ejemplo `ChatService` (o `ChatApiService`):

- Método `enviarMensaje(mensaje: string): Observable<ChatRespuestaRespDTO>`.
- Hace `POST` a `http://localhost:8080/api/chat/mensaje` (o a la URL base de tu API) con `{ mensaje }`.
- Incluir el token JWT en las cabeceras (igual que el resto de tus llamadas autenticadas).
- El backend devuelve `ChatRespuestaRespDTO`; el servicio solo devuelve ese observable.

### Paso 4.2 – Interfaz de respuesta

Definir en TypeScript algo equivalente a:

```ts
export interface ChatRespuestaRespDTO {
  respuesta: string;
  tipoAccion?: string;
  payload?: string;
}
```

### Paso 4.3 – Componente de chat (UI)

Crear un componente, por ejemplo `ChatComponent` o `ChatWidgetComponent`:

- **Estado:** lista de mensajes (array de `{ emisor: 'usuario' | 'bot', texto: string }`).
- **Input:** caja de texto + botón “Enviar” (o enviar con Enter).
- Al enviar:
  - Añadir el mensaje del usuario a la lista y mostrarlo.
  - Llamar al servicio `enviarMensaje(texto)`.
  - En el subscribe, añadir la respuesta del bot a la lista y mostrarla.
  - Manejar errores (token expirado, red, etc.) con un mensaje amigable en el chat o un toast.
- Opcional: si `tipoAccion === 'ver_pedido'` y hay `payload`, mostrar un enlace o botón “Ver pedido” que navegue a la ruta de detalle de pedido.

Puedes implementar el chat como:

- **Página dedicada:** ruta `/chat` y un `<router-outlet>`.
- **Widget flotante:** botón en la esquina que abre un panel con el historial y el input (mejor para no salir de la página actual).

### Paso 4.4 – Integrar en la app

- Si es página: añadir la ruta en tu `RouterModule` y un enlace en el menú o header.
- Si es widget: incluir el componente del chat en el layout principal (por ejemplo en `app.component.html`) para que se vea en todas las páginas.

---

## 5. Opcional: integración con IA (OpenAI u otra)

Si eliges **B** o **C**:

1. **Backend:**
   - Añadir dependencia para HTTP (por ejemplo `WebClient` o `RestTemplate`) si no la usas ya.
   - Crear un cliente que llame a la API de OpenAI (o similar) con el mensaje del usuario y un “system prompt” que describa que es el asistente de FixLab (productos, pedidos, contacto).
   - En `ChatServiceImpl`, si quieres híbrido: primero intentar reglas/FAQ; si no hay coincidencia, llamar al cliente de IA y devolver esa respuesta en `ChatRespuestaRespDTO`.
   - **Nunca** poner la API key en el código; usar `application.properties` / variables de entorno y leerlas con `@Value` o `@ConfigurationProperties`.

2. **Frontend:** no cambia; sigue enviando un mensaje y mostrando la respuesta.

3. **Costes y límites:** definir límite de mensajes por usuario/día si usas API de pago, y cachear respuestas frecuentes si aplica.

---

## 6. Resumen de tareas (checklist)

**Backend (fixlab-api):**

- [ ] Crear `ChatMensajeReqDTO` y `ChatRespuestaRespDTO`.
- [ ] Crear `ChatService` (interfaz) y `ChatServiceImpl` (lógica con reglas/FAQ y opcionalmente IA).
- [ ] Crear `ChatController` con `POST /api/chat/mensaje`.
- [ ] Dejar `/api/chat/**` protegido por JWT.
- [ ] (Opcional) Cliente HTTP para OpenAI y uso desde `ChatServiceImpl`.

**Frontend (fixlab-web):**

- [ ] Crear servicio que llame a `POST /api/chat/mensaje` con JWT.
- [ ] Definir interfaz `ChatRespuestaRespDTO`.
- [ ] Crear componente de chat (lista de mensajes + input + envío).
- [ ] Integrar como página `/chat` o widget flotante.
- [ ] (Opcional) Botón “Ver pedido” cuando `tipoAccion` y `payload` vengan en la respuesta.

---

## 7. Orden sugerido de implementación

1. Backend: DTOs → `ChatServiceImpl` solo con 2–3 respuestas fijas (ej. “hola” → “Hola, ¿en qué puedo ayudarte?”) → `ChatController` → probar con Postman o curl con JWT.
2. Frontend: servicio → componente mínimo (input + enviar + mostrar respuesta) → probar contra el backend.
3. Mejorar `ChatServiceImpl` con reglas para pedidos, productos y contacto usando tus servicios existentes.
4. Si lo deseas, añadir IA y luego el widget flotante o la página dedicada.

Si indicas en qué paso estás (solo backend, solo frontend, o ambos), puedo ayudarte con el código concreto de `ChatServiceImpl` o del componente Angular paso a paso.

---

## 8. Implementación realizada (opción C – híbrido)

Se implementó el chat híbrido en el proyecto:

**Backend (fixlab-api):**
- `dto/req/ChatMensajeReqDTO.java` y `dto/resp/ChatRespuestaRespDTO.java`
- `service/interfaces/ChatService.java` y `service/impl/ChatServiceImpl.java` (reglas: pedidos, productos, contacto, saludos; fallback OpenAI si está configurada)
- `service/impl/OpenAIClient.java` para llamar a la API de OpenAI (opcional)
- `controller/ChatController.java` con `POST /api/chat/mensaje` (requiere JWT)
- `config/OpenAIConfig.java` con bean `RestTemplate`
- En `application.properties`: `openai.api-key` y `openai.model` (opcionales)

**Frontend (fixlab-web):**
- `models/chat.model.ts`: interfaces del chat
- `services/chat.service.ts`: llama a `POST /api/chat/mensaje` (el interceptor añade el JWT)
- Componente `components/chat/` (chat.ts, chat.html, chat.css) con lista de mensajes, input y botones de acción (ver pedido, ver productos)
- Ruta `/chat` protegida con `authGuard` y enlace en el panel de cuenta del header

**Configuración opcional de OpenAI:** en `application.properties` o variables de entorno define `OPENAI_API_KEY` (y opcionalmente `OPENAI_MODEL`, por defecto `gpt-3.5-turbo`). Si no se define la clave, el bot responde solo con reglas/FAQ.
