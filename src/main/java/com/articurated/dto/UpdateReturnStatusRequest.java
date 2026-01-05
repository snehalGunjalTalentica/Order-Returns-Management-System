package com.articurated.dto;

import com.articurated.model.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReturnStatusRequest {
    
    @NotNull(message = "Status is required")
    private ReturnStatus status;
    
    private String managerNotes;
}



