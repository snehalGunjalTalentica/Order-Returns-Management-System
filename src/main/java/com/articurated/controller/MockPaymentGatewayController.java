package com.articurated.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mock-payment-gateway")
public class MockPaymentGatewayController {

    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> processRefund(@RequestBody Map<String, Object> request) {
        // Simulate payment gateway processing
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("refundId", "REF-" + System.currentTimeMillis());
        response.put("message", "Refund processed successfully");
        response.put("amount", request.get("amount"));
        
        // Simulate processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return ResponseEntity.ok(response);
    }
}




