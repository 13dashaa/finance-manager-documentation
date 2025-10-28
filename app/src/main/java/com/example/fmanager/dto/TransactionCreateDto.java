package com.example.fmanager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreateDto {
    private String description;
    @NotNull(message = "Amount can not be null")
    private double amount;
    @PastOrPresent(message = "The transaction date must be in past or present")
    private LocalDateTime date;
    @Min(value = 1, message = "Only one category ID must be provided")
    private Integer categoryId;
    @Min(value = 1, message = "Only one account ID must be provided")
    private Integer accountId;
}
