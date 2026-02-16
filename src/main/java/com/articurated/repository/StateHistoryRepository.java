package com.articurated.repository;

import com.articurated.model.StateHistory;
import com.articurated.model.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
    List<StateHistory> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(EntityType entityType, Long entityId);
}




