package com.articurated.controller;

import com.articurated.dto.*;
import com.articurated.service.ReturnService;
import com.articurated.service.StateHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnController {

    private final ReturnService returnService;
    private final StateHistoryService stateHistoryService;

    public ReturnController(ReturnService returnService, StateHistoryService stateHistoryService) {
        this.returnService = returnService;
        this.stateHistoryService = stateHistoryService;
    }

    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ReturnResponse> createReturn(@PathVariable Long orderId,
                                                      @Valid @RequestBody CreateReturnRequest request) {
        ReturnResponse response = returnService.createReturn(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ReturnResponse> getReturnById(@PathVariable Long id) {
        ReturnResponse response = returnService.getReturnById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/return-number/{returnNumber}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ReturnResponse> getReturnByReturnNumber(@PathVariable String returnNumber) {
        ReturnResponse response = returnService.getReturnByReturnNumber(returnNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ReturnResponse>> getReturnsByOrder(@PathVariable Long orderId) {
        List<ReturnResponse> responses = returnService.getReturnsByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ReturnResponse> updateReturnStatus(@PathVariable Long id,
                                                             @Valid @RequestBody UpdateReturnStatusRequest request) {
        ReturnResponse response = returnService.updateReturnStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<StateHistoryResponse>> getReturnHistory(@PathVariable Long id) {
        List<StateHistoryResponse> history = stateHistoryService.getStateHistory(
                com.articurated.model.enums.EntityType.RETURN, id);
        return ResponseEntity.ok(history);
    }
}




