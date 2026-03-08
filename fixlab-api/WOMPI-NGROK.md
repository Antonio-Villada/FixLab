# Probar Wompi en local con ngrok

## Tu configuración actual

- **Túnel ngrok (backend):** `https://overproficient-karin-unrefrigerated.ngrok-free.dev` → `http://localhost:8081`
- **URL de webhook para Wompi:**  
  `https://overproficient-karin-unrefrigerated.ngrok-free.dev/api/webhooks/wompi`

En el dashboard de Wompi (Colombia test) la URL de notificación debe ser exactamente esa.

---

## Pasos para probar

1. **Arrancar el backend** (puerto 8081).
2. **Arrancar ngrok** al puerto 8081:
   ```bash
   ngrok http 8081
   ```
   Si tu URL cambia (plan free), actualiza en Wompi la URL de webhook y la propiedad `wompi.webhook-url` en `application.properties`.
3. **Arrancar el frontend:** `ng serve` (puerto 4200).
4. **Abrir la app** en el navegador: `http://localhost:4200`.
5. Iniciar sesión, añadir productos al carrito, ir a Carrito → Dirección de envío → **Proceder a pagar (Wompi)**.
6. Completar el pago en test con la tarjeta de prueba (ej. `4242 4242 4242 4242`).
7. Wompi enviará el webhook a tu URL ngrok → tu backend marcará el pedido como PAGADO. Puedes ver el pedido en **Mis compras** o en Admin → Pedidos.

---

## Si aparece 403 o 483 al abrir el checkout

Wompi rechaza `localhost` en la URL de redirección. Para que funcione con ngrok:

1. **Segundo túnel ngrok al frontend:** `ngrok http 4200` (en otra terminal).
2. En `fixlab-web/src/environments/environment.ts` pon en `appBaseUrlForWompi` la URL de ESE túnel (ej. `https://xyz.ngrok-free.dev`).
3. Abre la app en el navegador por esa URL (no por localhost).
4. Haz el pago: Wompi te redirigirá a `/pago-exitoso` en tu app.

---

## Resumen

| Qué              | Dónde / Cómo |
|------------------|----------------|
| Webhook Wompi    | URL en dashboard Wompi = `https://TU-NGROK.ngrok-free.dev/api/webhooks/wompi` |
| Backend          | Corriendo en `localhost:8081`; ngrok reenvía a 8081 |
| Frontend (API)   | Sigue usando `apiBaseUrl: 'http://localhost:8081'` en desarrollo |
| Cambio de red    | Si reinicias ngrok y cambia la URL, actualiza la URL en Wompi y en `wompi.webhook-url` |
