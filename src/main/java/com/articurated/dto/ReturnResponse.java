package com.articurated.dto;

import com.articurated.model.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    private Long id;
    private String returnNumber;
    private Long orderId;
    private ReturnStatus status;
    private String returnReason;
    private BigDecimal refundAmount;
    private String managerNotes;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;
}



