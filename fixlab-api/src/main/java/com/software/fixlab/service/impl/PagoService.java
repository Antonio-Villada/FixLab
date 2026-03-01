package com.software.fixlab.service.impl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.software.fixlab.entity.DetallePedido;
import com.software.fixlab.entity.Pedido;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    // Inicializamos Mercado Pago justo después de que Spring Boot arranque
    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public String crearPreferenciaPago(Pedido pedido) throws Exception {

        List<PreferenceItemRequest> items = new ArrayList<>();

        for (DetallePedido detalle : pedido.getDetalles()) {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(detalle.getProducto().getNombre())
                    .quantity(detalle.getCantidad())
                    .unitPrice(new BigDecimal(detalle.getPrecioUnitario()))
                    .currencyId("COP")
                    .build();
            items.add(itemRequest);
        }

        // 1. Definimos las URLs de retorno
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:8080/api/ventas/pago-exitoso")
                .failure("http://localhost:8080/api/ventas/pago-fallido")
                .pending("http://localhost:8080/api/ventas/pago-pendiente")
                .build();

        // 2. Armamos la petición asegurándonos de INCLUIR las backUrls
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls) // <-- ESTA ES LA LÍNEA QUE MERCADO PAGO ESTABA EXIGIENDO
//                .autoReturn("approved")   ACTIVAR EN DESPLIEGE
                .externalReference(pedido.getId().toString())
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint();

        } catch (com.mercadopago.exceptions.MPApiException e) {
            System.out.println("\n================ ERROR EXACTO DE MERCADO PAGO ================");
            System.out.println(e.getApiResponse().getContent());
            System.out.println("==============================================================\n");
            throw new Exception("La pasarela rechazó el pago. Revisa la consola de Ubuntu.");
        }
    }
}