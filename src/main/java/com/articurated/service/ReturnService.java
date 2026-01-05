package com.articurated.service;

import com.articurated.dto.*;
import com.articurated.exception.ResourceNotFoundException;
import com.articurated.model.Order;
import com.articurated.model.Return;
import com.articurated.model.enums.OrderStatus;
import com.articurated.model.enums.ReturnStatus;
import com.articurated.repository.OrderRepository;
import com.articurated.repository.ReturnRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final ReturnStateMachine stateMachine;
    private final StateHistoryService stateHistoryService;

    public ReturnService(ReturnRepository returnRepository, OrderRepository orderRepository,
                        ReturnStateMachine stateMachine, StateHistoryService stateHistoryService) {
        this.returnRepository = returnRepository;
        this.orderRepository = orderRepository;
        this.stateMachine = stateMachine;
        this.stateHistoryService = stateHistoryService;
    }

    @Transactional
    public ReturnResponse createReturn(Long orderId, CreateReturnRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Return can only be initiated for orders in DELIVERED status");
        }

        // Check if return is within 7 days of delivery
        if (order.getUpdatedAt() != null) {
            long daysSinceDelivery = ChronoUnit.DAYS.between(order.getUpdatedAt(), LocalDateTime.now());
            if (daysSinceDelivery > 7) {
                throw new IllegalArgumentException("Return request must be made within 7 days of delivery");
            }
        }

        // Check if return already exists for this order
        List<Return> existingReturns = returnRepository.findByOrderId(orderId);
        boolean hasActiveReturn = existingReturns.stream()
                .anyMatch(r -> r.getStatus() != ReturnStatus.REJECTED && r.getStatus() != ReturnStatus.COMPLETED);
        if (hasActiveReturn) {
            throw new IllegalArgumentException("An active return already exists for this order");
        }

        Return returnRequest = new Return();
        returnRequest.setOrder(order);
        returnRequest.setReturnNumber(generateReturnNumber());
        returnRequest.setStatus(ReturnStatus.REQUESTED);
        returnRequest.setReturnReason(request.getReturnReason());
        returnRequest.setRefundAmount(order.getTotalAmount()); // Full refund

        Return savedReturn = returnRepository.save(returnRequest);
        logStateChange(savedReturn, null, ReturnStatus.REQUESTED, getCurrentUserEmail(), "Return requested");

        return mapToReturnResponse(savedReturn);
    }

    public ReturnResponse getReturnById(Long returnId) {
        Return returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + returnId));
        return mapToReturnResponse(returnRequest);
    }

    public ReturnResponse getReturnByReturnNumber(String returnNumber) {
        Return returnRequest = returnRepository.findByReturnNumber(returnNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with return number: " + returnNumber));
        return mapToReturnResponse(returnRequest);
    }

    public List<ReturnResponse> getReturnsByOrderId(Long orderId) {
        return returnRepository.findByOrderId(orderId).stream()
                .map(this::mapToReturnResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReturnResponse updateReturnStatus(Long returnId, UpdateReturnStatusRequest request) {
        Return returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + returnId));

        ReturnStatus oldStatus = returnRequest.getStatus();
        stateMachine.validateTransition(oldStatus, request.getStatus());

        returnRequest.setStatus(request.getStatus());
        if (request.getManagerNotes() != null) {
            returnRequest.setManagerNotes(request.getManagerNotes());
        }

        Return updatedReturn = returnRepository.save(returnRequest);
        logStateChange(updatedReturn, oldStatus, request.getStatus(), getCurrentUserEmail(), request.getManagerNotes());

        // Trigger background job for refund processing when return is completed
        if (request.getStatus() == ReturnStatus.COMPLETED) {
            // This will be handled by Spring Batch job
        }

        return mapToReturnResponse(updatedReturn);
    }

    private String generateReturnNumber() {
        return "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void logStateChange(Return returnRequest, ReturnStatus oldStatus, ReturnStatus newStatus, String changedBy, String reason) {
        stateHistoryService.logStateChange(
                com.articurated.model.enums.EntityType.RETURN,
                returnRequest.getId(),
                oldStatus != null ? oldStatus.name() : null,
                newStatus.name(),
                changedBy,
                reason
        );
    }

    private ReturnResponse mapToReturnResponse(Return returnRequest) {
        ReturnResponse response = new ReturnResponse();
        response.setId(returnRequest.getId());
        response.setReturnNumber(returnRequest.getReturnNumber());
        response.setOrderId(returnRequest.getOrder().getId());
        response.setStatus(returnRequest.getStatus());
        response.setReturnReason(returnRequest.getReturnReason());
        response.setRefundAmount(returnRequest.getRefundAmount());
        response.setManagerNotes(returnRequest.getManagerNotes());
        response.setRequestedAt(returnRequest.getRequestedAt());
        response.setUpdatedAt(returnRequest.getUpdatedAt());
        return response;
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}

