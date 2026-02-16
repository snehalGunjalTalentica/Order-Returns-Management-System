package com.articurated.service;

import com.articurated.exception.InvalidStateTransitionException;
import com.articurated.model.enums.ReturnStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ReturnStateMachineTest {

    private ReturnStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new ReturnStateMachine();
    }

    @Test
    @DisplayName("Should allow valid transition from REQUESTED to APPROVED")
    void testValidTransitionRequestedToApproved() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(ReturnStatus.REQUESTED, ReturnStatus.APPROVED)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from REQUESTED to REJECTED")
    void testValidTransitionRequestedToRejected() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(ReturnStatus.REQUESTED, ReturnStatus.REJECTED)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from APPROVED to IN_TRANSIT")
    void testValidTransitionApprovedToInTransit() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(ReturnStatus.APPROVED, ReturnStatus.IN_TRANSIT)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from IN_TRANSIT to RECEIVED")
    void testValidTransitionInTransitToReceived() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(ReturnStatus.IN_TRANSIT, ReturnStatus.RECEIVED)
        );
    }

    @Test
    @DisplayName("Should allow valid transition from RECEIVED to COMPLETED")
    void testValidTransitionReceivedToCompleted() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(ReturnStatus.RECEIVED, ReturnStatus.COMPLETED)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from REJECTED to APPROVED")
    void testInvalidTransitionRejectedToApproved() {
        assertThrows(InvalidStateTransitionException.class, () -> 
            stateMachine.validateTransition(ReturnStatus.REJECTED, ReturnStatus.APPROVED)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from COMPLETED to RECEIVED")
    void testInvalidTransitionCompletedToReceived() {
        assertThrows(InvalidStateTransitionException.class, () -> 
            stateMachine.validateTransition(ReturnStatus.COMPLETED, ReturnStatus.RECEIVED)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from REQUESTED to COMPLETED")
    void testInvalidTransitionRequestedToCompleted() {
        assertThrows(InvalidStateTransitionException.class, () -> 
            stateMachine.validateTransition(ReturnStatus.REQUESTED, ReturnStatus.COMPLETED)
        );
    }

    @Test
    @DisplayName("Should allow same state transition")
    void testSameStateTransition() {
        assertDoesNotThrow(() -> 
            stateMachine.validateTransition(ReturnStatus.APPROVED, ReturnStatus.APPROVED)
        );
    }
}


