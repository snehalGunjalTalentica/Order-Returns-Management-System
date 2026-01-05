package com.articurated.service;

import com.articurated.model.StateHistory;
import com.articurated.model.enums.EntityType;
import com.articurated.repository.StateHistoryRepository;
import com.articurated.model.StateHistory;
import com.articurated.model.enums.EntityType;
import com.articurated.repository.StateHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StateHistoryServiceTest {

    @Mock
    private StateHistoryRepository stateHistoryRepository;

    @InjectMocks
    private StateHistoryService stateHistoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should log state change for order")
    void testLogStateChangeForOrder() {
        // Given
        EntityType entityType = EntityType.ORDER;
        Long entityId = 1L;
        String previousStatus = "PENDING_PAYMENT";
        String newStatus = "PAID";
        String changedBy = "admin@example.com";
        String changeReason = "Payment confirmed";

        when(stateHistoryRepository.save(any(StateHistory.class))).thenReturn(new StateHistory());

        // When
        stateHistoryService.logStateChange(entityType, entityId, previousStatus, newStatus, changedBy, changeReason);

        // Then
        verify(stateHistoryRepository, times(1)).save(any(StateHistory.class));
    }

    @Test
    @DisplayName("Should log state change for return")
    void testLogStateChangeForReturn() {
        // Given
        EntityType entityType = EntityType.RETURN;
        Long entityId = 1L;
        String previousStatus = "REQUESTED";
        String newStatus = "APPROVED";
        String changedBy = "manager@example.com";
        String changeReason = "Return approved";

        when(stateHistoryRepository.save(any(StateHistory.class))).thenReturn(new StateHistory());

        // When
        stateHistoryService.logStateChange(entityType, entityId, previousStatus, newStatus, changedBy, changeReason);

        // Then
        verify(stateHistoryRepository, times(1)).save(any(StateHistory.class));
    }

    @Test
    @DisplayName("Should handle null previous status")
    void testLogStateChangeWithNullPreviousStatus() {
        // Given
        EntityType entityType = EntityType.ORDER;
        Long entityId = 1L;
        String previousStatus = null;
        String newStatus = "PENDING_PAYMENT";
        String changedBy = "system";
        String changeReason = "Order created";

        when(stateHistoryRepository.save(any(StateHistory.class))).thenReturn(new StateHistory());

        // When
        stateHistoryService.logStateChange(entityType, entityId, previousStatus, newStatus, changedBy, changeReason);

        // Then
        verify(stateHistoryRepository, times(1)).save(any(StateHistory.class));
    }
}

