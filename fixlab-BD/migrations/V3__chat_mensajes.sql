-- Historial de chat del asistente por usuario (email = identidad en JWT)
CREATE TABLE IF NOT EXISTS chat_mensaje (
    id BIGSERIAL PRIMARY KEY,
    usuario_email VARCHAR(255) NOT NULL REFERENCES usuarios (email) ON DELETE CASCADE,
    texto TEXT NOT NULL,
    rol VARCHAR(10) NOT NULL CHECK (rol IN ('USER', 'BOT')),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_mensaje_usuario_creado ON chat_mensaje (usuario_email, creado_en);

COMMENT ON TABLE chat_mensaje IS 'Mensajes del asistente FixLab por usuario';
