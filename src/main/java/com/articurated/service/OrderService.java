package com.articurated.service;

import com.articurated.dto.*;
import com.articurated.exception.ResourceNotFoundException;
import com.articurated.model.Customer;
import com.articurated.model.Order;
import com.articurated.model.OrderItem;
import com.articurated.model.enums.OrderStatus;
import com.articurated.model.enums.Role;
import com.articurated.repository.CustomerRepository;
import com.articurated.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderStateMachine stateMachine;
    private final StateHistoryService stateHistoryService;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
                       OrderStateMachine stateMachine, StateHistoryService stateHistoryService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.stateMachine = stateMachine;
        this.stateHistoryService = stateHistoryService;
    }

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentTransactionId(request.getPaymentTransactionId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDto itemDto : request.getOrderItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemDto.getProductId());
            item.setProductName(itemDto.getProductName());
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            item.setTotalPrice(itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            order.getOrderItems().add(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        logStateChange(savedOrder, null, OrderStatus.PENDING_PAYMENT, getCurrentUserEmail(), "Order created");

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToOrderResponse(order);
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        stateMachine.validateTransition(oldStatus, request.getStatus());

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);

        logStateChange(updatedOrder, oldStatus, request.getStatus(), getCurrentUserEmail(), request.getReason());

        // Trigger background job for PDF generation when order is shipped
        if (request.getStatus() == OrderStatus.SHIPPED) {
            // This will be handled by Spring Batch job - trigger it asynchronously
            // Note: In a real implementation, you might want to use @Async or message queue
        }

        return mapToOrderResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!stateMachine.canCancel(order.getStatus())) {
            throw new IllegalArgumentException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        logStateChange(updatedOrder, oldStatus, OrderStatus.CANCELLED, getCurrentUserEmail(), reason);

        return mapToOrderResponse(updatedOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void logStateChange(Order order, OrderStatus oldStatus, OrderStatus newStatus, String changedBy, String reason) {
        stateHistoryService.logStateChange(
                com.articurated.model.enums.EntityType.ORDER,
                order.getId(),
                oldStatus != null ? oldStatus.name() : null,
                newStatus.name(),
                changedBy,
                reason
        );
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerId(order.getCustomer().getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentTransactionId(order.getPaymentTransactionId());
        response.setOrderItems(order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList()));
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        return response;
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}

