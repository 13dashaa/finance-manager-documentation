package com.example.fmanager.dto;

import com.example.fmanager.models.Account;
import com.example.fmanager.models.Transaction;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountGetDto {

    private Integer id;
    private String name;
    private Integer clientId;
    private double balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String clientUsername;
    private Set<String> transactionDescriptions;

    public static AccountGetDto convertToDto(Account account) {
        AccountGetDto dto = new AccountGetDto();
        dto.setId(account.getId());
        Set<Transaction> transactions = account.getTransactions();
        if (transactions != null) {
            dto.setTransactionDescriptions(account.getTransactions().stream()
                    .map(Transaction::getDescription)
                    .collect(Collectors.toSet()));
        } else {
            dto.setTransactionDescriptions(new HashSet<>());
        }
        dto.setClientUsername(account.getClient().getUsername());
        dto.setName(account.getName());
        dto.setClientId(account.getClient().getId());
        dto.setBalance(account.getBalance());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}