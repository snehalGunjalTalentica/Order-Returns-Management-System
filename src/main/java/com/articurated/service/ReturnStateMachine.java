package com.articurated.service;

import com.articurated.model.enums.ReturnStatus;
import com.articurated.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class ReturnStateMachine {

    public void validateTransition(ReturnStatus currentStatus, ReturnStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // Same state is allowed
        }

        Set<ReturnStatus> validTransitions = getValidTransitions(currentStatus);
        
        if (!validTransitions.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                String.format("Invalid transition from %s to %s", currentStatus, newStatus));
        }
    }

    private Set<ReturnStatus> getValidTransitions(ReturnStatus currentStatus) {
        return switch (currentStatus) {
            case REQUESTED -> EnumSet.of(ReturnStatus.APPROVED, ReturnStatus.REJECTED);
            case APPROVED -> EnumSet.of(ReturnStatus.IN_TRANSIT);
            case REJECTED -> EnumSet.noneOf(ReturnStatus.class); // No transitions from REJECTED
            case IN_TRANSIT -> EnumSet.of(ReturnStatus.RECEIVED);
            case RECEIVED -> EnumSet.of(ReturnStatus.COMPLETED);
            case COMPLETED -> EnumSet.noneOf(ReturnStatus.class); // No transitions from COMPLETED
        };
    }
}



