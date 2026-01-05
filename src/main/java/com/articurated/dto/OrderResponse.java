package com.articurated.dto;

import com.articurated.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private String paymentTransactionId;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



