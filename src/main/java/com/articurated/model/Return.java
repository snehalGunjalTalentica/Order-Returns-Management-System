package com.articurated.model;

import com.articurated.model.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Return {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "return_number", nullable = false, unique = true)
    private String returnNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status = ReturnStatus.REQUESTED;
    
    @Column(name = "return_reason", nullable = false, columnDefinition = "TEXT")
    private String returnReason;
    
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "manager_notes", columnDefinition = "TEXT")
    private String managerNotes;
    
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



