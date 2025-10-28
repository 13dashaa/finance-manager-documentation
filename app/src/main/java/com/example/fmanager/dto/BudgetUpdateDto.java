package com.example.fmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class BudgetUpdateDto {
    @NotNull(message = "Period cannot be null")
    private int period;
    @NotNull(message = "Limitation cannot be null")
    @Positive(message = "Limitation cannot be negative")
    private double limitation;
    @Size(min = 1, message = "At least one client ID must be provided")
    private Set<Integer> clientIds;
}

