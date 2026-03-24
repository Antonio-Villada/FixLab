-- Campos para recuperación de contraseña por código (en vez de enlace)
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS codigo_recuperacion VARCHAR(6);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS expiracion_codigo_recuperacion TIMESTAMP;
COMMENT ON COLUMN usuarios.codigo_recuperacion IS 'Código de 6 dígitos enviado por correo para restablecer contraseña';
COMMENT ON COLUMN usuarios.expiracion_codigo_recuperacion IS 'Expiración del código de recuperación (ej: 15 min)';
