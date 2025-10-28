package com.example.fmanager.dto;

import com.example.fmanager.models.Transaction;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionGetDto {
    private Integer id;
    private String description;
    private double amount;
    private Integer accountId;
    private String accountName;
    private Integer categoryId;
    private String categoryName;
    private LocalDateTime date;
    private LocalDateTime createdAt;

    public static TransactionGetDto convertToDto(Transaction transaction) {
        TransactionGetDto dto = new TransactionGetDto();
        dto.setId(transaction.getId());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setAccountId(transaction.getAccount().getId());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getDate());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setCategoryName(transaction.getCategory().getName());
        dto.setAccountName(transaction.getAccount().getName());
        return dto;
    }
}
