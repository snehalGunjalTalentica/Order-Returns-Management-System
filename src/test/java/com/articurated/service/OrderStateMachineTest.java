package com.articurated.service;

import com.articurated.exception.InvalidStateTransitionException;
import com.articurated.model.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateMachineTest {

    private OrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new OrderStateMachine();
    }

    @Test
    @DisplayName("Should allow valid transition from PENDING_PAYMENT to PAID")
    void testValidTransitionPendingPaymentToPaid() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from PENDING_PAYMENT to CANCELLED")
    void testValidTransitionPendingPaymentToCancelled() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from PAID to PROCESSING_IN_WAREHOUSE")
    void testValidTransitionPaidToProcessing() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(OrderStatus.PAID, OrderStatus.PROCESSING_IN_WAREHOUSE)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from PROCESSING_IN_WAREHOUSE to SHIPPED")
    void testValidTransitionProcessingToShipped() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(OrderStatus.PROCESSING_IN_WAREHOUSE, OrderStatus.SHIPPED)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from SHIPPED to DELIVERED")
    void testValidTransitionShippedToDelivered() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(OrderStatus.SHIPPED, OrderStatus.DELIVERED)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from DELIVERED to PAID")
    void testInvalidTransitionDeliveredToPaid() {
        assertThrows(InvalidStateTransitionException.class, () -> 
            stateMachine.validateTransition(OrderStatus.DELIVERED, OrderStatus.PAID)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from CANCELLED to PAID")
    void testInvalidTransitionCancelledToPaid() {
        assertThrows(InvalidStateTransitionException.class, () -> 
            stateMachine.validateTransition(OrderStatus.CANCELLED, OrderStatus.PAID)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from SHIPPED to CANCELLED")
    void testInvalidTransitionShippedToCancelled() {
        assertThrows(InvalidStateTransitionException.class, () -> 
            stateMachine.validateTransition(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("Should allow same state transition")
    void testSameStateTransition() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(OrderStatus.PAID, OrderStatus.PAID)
        );
    }

    @Test
    @DisplayName("Should return true for cancellable status PENDING_PAYMENT")
    void testCanCancelPendingPayment() {
        assertTrue(stateMachine.canCancel(OrderStatus.PENDING_PAYMENT));
    }

    @Test
    @DisplayName("Should return true for cancellable status PAID")
    void testCanCancelPaid() {
        assertTrue(stateMachine.canCancel(OrderStatus.PAID));
    }

    @Test
    @DisplayName("Should return false for non-cancellable status SHIPPED")
    void testCannotCancelShipped() {
        assertFalse(stateMachine.canCancel(OrderStatus.SHIPPED));
    }

    @Test
    @DisplayName("Should return false for non-cancellable status DELIVERED")
    void testCannotCancelDelivered() {
        assertFalse(stateMachine.canCancel(OrderStatus.DELIVERED));
    }
}


