package com.articurated.service;

import com.articurated.dto.StateHistoryResponse;
import com.articurated.model.StateHistory;
import com.articurated.model.enums.EntityType;
import com.articurated.repository.StateHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StateHistoryService {

    private final StateHistoryRepository stateHistoryRepository;

    public StateHistoryService(StateHistoryRepository stateHistoryRepository) {
        this.stateHistoryRepository = stateHistoryRepository;
    }

    @Transactional
    public void logStateChange(EntityType entityType, Long entityId, String previousStatus, 
                              String newStatus, String changedBy, String changeReason) {
        StateHistory history = new StateHistory();
        history.setEntityType(entityType);
        history.setEntityId(entityId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangeReason(changeReason);
        
        stateHistoryRepository.save(history);
    }

    public List<StateHistoryResponse> getStateHistory(EntityType entityType, Long entityId) {
        List<StateHistory> historyList = stateHistoryRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        
        return historyList.stream()
                .map(this::mapToStateHistoryResponse)
                .collect(Collectors.toList());
    }

    private StateHistoryResponse mapToStateHistoryResponse(StateHistory history) {
        StateHistoryResponse response = new StateHistoryResponse();
        response.setId(history.getId());
        response.setEntityType(history.getEntityType().name());
        response.setEntityId(history.getEntityId());
        response.setPreviousStatus(history.getPreviousStatus());
        response.setNewStatus(history.getNewStatus());
        response.setChangedBy(history.getChangedBy());
        response.setChangeReason(history.getChangeReason());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }
}

