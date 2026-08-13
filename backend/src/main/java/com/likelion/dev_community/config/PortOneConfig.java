package com.likelion.dev_community.config;

import io.portone.sdk.server.payment.PaymentClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortOneConfig {

    @Value("${portone.api-secret}")
    private String apiSecret;

    @Bean
    public PaymentClient paymentClient() {
        return new PaymentClient(apiSecret, "https://api.portone.io", null);
    }
}
