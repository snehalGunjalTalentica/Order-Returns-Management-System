package com.articurated.controller;

import com.articurated.dto.*;
import com.articurated.repository.CustomerRepository;
import com.articurated.service.OrderService;
import com.articurated.service.StateHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final StateHistoryService stateHistoryService;
    private final CustomerRepository customerRepository;

    public OrderController(OrderService orderService, StateHistoryService stateHistoryService,
                          CustomerRepository customerRepository) {
        this.orderService = orderService;
        this.stateHistoryService = stateHistoryService;
        this.customerRepository = customerRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                     Authentication authentication) {
        // Get customer ID from authentication (simplified - in real app, extract from token)
        Long customerId = getCustomerIdFromAuth(authentication);
        OrderResponse response = orderService.createOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order-number/{orderNumber}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderResponse> responses = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id,
                                                     @RequestParam(required = false) String reason) {
        OrderResponse response = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<StateHistoryResponse>> getOrderHistory(@PathVariable Long id) {
        List<StateHistoryResponse> history = stateHistoryService.getStateHistory(
                com.articurated.model.enums.EntityType.ORDER, id);
        return ResponseEntity.ok(history);
    }

    private Long getCustomerIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .map(customer -> customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found for email: " + email));
    }
}

