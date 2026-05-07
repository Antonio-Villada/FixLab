# FixLab Chat-bot (Python, internal)

Microservicio **interno** para que `fixlab-api` delegue la generación de respuestas del chat sin cambiar el front (Angular sigue llamando a `/api/chat/*`).

## Ejecutar en local

En PowerShell:

```powershell
cd C:\FixLab\Chat-bot
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --host 127.0.0.1 --port 8090 --reload
```

## Ejecutar con Docker

```powershell
cd C:\FixLab\Chat-bot
docker build -t fixlab-chatbot:local .
# Sin pegar la API key en la imagen: pásala por entorno en tiempo de ejecución.
docker run --rm -p 8090:8090 `
  -e FIXLAB_CHAT_MODE=gemini `
  -e GEMINI_API_KEY=TU_CLAVE_AQUI `
  fixlab-chatbot:local
```

Health:
- `GET /health`

Responder:
- `POST /reply`

## Variables de entorno

- **`FIXLAB_CHAT_MODE`**: modo del bot (por defecto `rules`).
  - `rules`: respuestas por reglas / FAQ (sin llamada externa).
  - `gemini`: usa la API de **Google Gemini** (misma familia de endpoints que `fixlab-api`).
  - `auto`: si existe `GEMINI_API_KEY` (o `GOOGLE_API_KEY`), equivale a `gemini`; si no, a `rules`.

- **`GEMINI_API_KEY`**: clave de [Google AI Studio](https://aistudio.google.com/apikey). Alternativa: `GOOGLE_API_KEY`.

- **`GEMINI_MODEL`**: modelo (por defecto `gemini-2.5-flash-lite`, alineado con `application.properties` del backend).

### Ejemplo en PowerShell (Gemini)

Antes de arrancar uvicorn:

```powershell
$env:FIXLAB_CHAT_MODE = "gemini"
$env:GEMINI_API_KEY = "tu-clave-solo-en-tu-equipo"
# opcional:
$env:GEMINI_MODEL = "gemini-2.5-flash-lite"
python -m uvicorn main:app --host 127.0.0.1 --port 8090 --reload
```

No subas la clave al repositorio ni la pongas en capturas; usa solo variables de entorno o secretos del servidor.

## Gemini no responde (caída silenciosa al FAQ por reglas)

1. Abre `GET http://127.0.0.1:8090/health` y revisa:
   - `effective_mode` debe ser `gemini`
   - `gemini_key_configured` debe ser `true`
   - Si `fixlab_chat_mode` es `rules`, Gemini **no se usa** (no basta tener la key).
   - Tras al menos un mensaje al chat, si Gemini falla verás `last_gemini` con `error_preview` y `http_status` (fragmento del error de Google: modelo, clave, cuota, etc.). Si no aparece, aún no hubo llamadas desde el arranque del proceso.

2. Las variables deben existir **en el mismo proceso** que ejecuta uvicorn. Si arrancas el servidor desde el IDE sin cargar `.env`, no verán la key.

3. Mira la **consola donde corre uvicorn**: ante fallos HTTP o respuestas sin texto verás líneas `WARNING` del logger `fixlab.chatbot` con `status` y un fragmento del cuerpo de error de Google (clave inválida, modelo incorrecto, cuota, etc.).

4. Si el modelo falla, prueba otro id estable, por ejemplo `GEMINI_MODEL=gemini-2.0-flash`.

5. Antes se leía solo la primera `part` de la respuesta; algunos modelos devuelven varias partes (p. ej. thinking). El código ahora **concatena todas las partes con texto visible**.

### Error 429 / cuota free tier

Si en `/health` ves `http_status: 429` y mensajes como `Quota exceeded` o `generate_content_free_tier_requests`:

- Es el **límite de la capa gratuita** de Google AI (por ejemplo ~20 solicitudes por ventana de tiempo para ciertos modelos). No es un fallo de tu código.
- Espera el tiempo que indica el mensaje (`Please retry in …s`) o revisa [límites y facturación](https://ai.google.dev/gemini-api/docs/rate-limits).
- Opciones: activar **facturación** en el proyecto de Google AI Studio, usar **otro modelo** (`GEMINI_MODEL=gemini-2.0-flash`, puede tener otro cupo), o bajar pruebas repetidas.
- El chatbot ya **no encadena más reintentos** tras un 429 para no gastar la cuota varias veces en un solo mensaje.

