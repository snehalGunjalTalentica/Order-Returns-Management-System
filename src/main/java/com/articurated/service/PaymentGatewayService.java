package com.articurated.service;

import com.articurated.model.Return;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentGatewayService {

    private final WebClient webClient;
    private final String paymentGatewayUrl;

    public PaymentGatewayService(@Value("${payment.gateway.url}") String paymentGatewayUrl) {
        this.paymentGatewayUrl = paymentGatewayUrl;
        this.webClient = WebClient.builder()
                .baseUrl(paymentGatewayUrl)
                .build();
    }

    public void processRefund(Return returnRequest) {
        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("orderId", returnRequest.getOrder().getId());
        refundRequest.put("returnId", returnRequest.getId());
        refundRequest.put("amount", returnRequest.getRefundAmount());
        refundRequest.put("paymentTransactionId", returnRequest.getOrder().getPaymentTransactionId());

        try {
            String response = webClient.post()
                    .uri("/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(refundRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            System.out.println("Refund processed successfully for return: " + returnRequest.getReturnNumber());
            System.out.println("Payment gateway response: " + response);
        } catch (Exception e) {
            System.err.println("Error processing refund for return " + returnRequest.getReturnNumber() + ": " + e.getMessage());
            throw new RuntimeException("Failed to process refund", e);
        }
    }
}




