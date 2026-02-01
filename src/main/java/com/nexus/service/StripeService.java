package com.nexus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService {

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.currency:eur}")
    private String moneda;

    @PostConstruct
    public void init() {
        if (stripeApiKey != null && !stripeApiKey.isEmpty()) {
            Stripe.apiKey = stripeApiKey;
        }
    }

    public PaymentIntent crearIntentoPago(Double cantidad, String descripcion) throws Exception {
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            throw new Exception("Stripe API Key no configurada");
        }

        // Stripe funciona con céntimos (10.00€ -> 1000 cents)
        long cantidadCentimos = (long) (cantidad * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(cantidadCentimos)
                .setCurrency(moneda)
                .setDescription(descripcion)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        return PaymentIntent.create(params);
    }
}