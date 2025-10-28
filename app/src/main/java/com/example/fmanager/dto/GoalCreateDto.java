package com.example.fmanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoalCreateDto {
    @NotBlank(message = "Name cannot be null")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    @NotNull(message = "Target amount cannot be null")
    @Positive(message = "Target amount must be positive")
    private double targetAmount;
    private double currentAmount;
    private LocalDate startDate;
    @NotNull(message = "End date cannot be null")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    @Min(value = 1, message = "Only one client ID must be provided")
    private Integer clientId;

    public GoalCreateDto(String name,
                         double targetAmount,
                         LocalDate startDate,
                         LocalDate endDate,
                         int clientId) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.clientId = clientId;
    }
}
