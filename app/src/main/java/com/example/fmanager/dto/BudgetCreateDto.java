package com.example.fmanager.dto;

import jakarta.validation.constraints.Min;
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
public class BudgetCreateDto {
    @NotNull(message = "Period cannot be null")
    private Integer period;
    @NotNull(message = "Limitation cannot be null")
    @Positive(message = "Limitation cannot be negative")
    private double limitation;
    @Min(value = 1, message = "Only one category ID must be provided")
    private Integer categoryId;
    @Size(min = 1, message = "At least one client ID must be provided")
    private Set<Integer> clientIds;
}
