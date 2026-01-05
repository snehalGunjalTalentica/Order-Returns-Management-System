package com.articurated.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateHistoryResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private String changeReason;
    private LocalDateTime createdAt;
}



