package com.articurated.service;

import com.articurated.model.Order;
import com.articurated.model.enums.OrderStatus;
import com.articurated.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class OrderStateMachine {

    public void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // Same state is allowed
        }

        Set<OrderStatus> validTransitions = getValidTransitions(currentStatus);
        
        if (!validTransitions.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                String.format("Invalid transition from %s to %s", currentStatus, newStatus));
        }
    }

    private Set<OrderStatus> getValidTransitions(OrderStatus currentStatus) {
        return switch (currentStatus) {
            case PENDING_PAYMENT -> EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID -> EnumSet.of(OrderStatus.PROCESSING_IN_WAREHOUSE, OrderStatus.CANCELLED);
            case PROCESSING_IN_WAREHOUSE -> EnumSet.of(OrderStatus.SHIPPED);
            case SHIPPED -> EnumSet.of(OrderStatus.DELIVERED);
            case DELIVERED -> EnumSet.noneOf(OrderStatus.class); // No transitions from DELIVERED
            case CANCELLED -> EnumSet.noneOf(OrderStatus.class); // No transitions from CANCELLED
        };
    }

    public boolean canCancel(OrderStatus status) {
        return status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAID;
    }
}



