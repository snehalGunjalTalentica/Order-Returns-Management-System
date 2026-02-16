package com.articurated.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {
    
    @NotBlank(message = "Return reason is required")
    private String returnReason;
}




