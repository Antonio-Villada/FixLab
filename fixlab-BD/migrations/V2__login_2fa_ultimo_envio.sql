ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ultimo_envio_codigo_login_2fa TIMESTAMP;
COMMENT ON COLUMN usuarios.ultimo_envio_codigo_login_2fa IS 'Momento del último envío del código de acceso por email';
