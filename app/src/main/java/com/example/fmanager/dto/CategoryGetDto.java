package com.example.fmanager.dto;

import com.example.fmanager.models.Budget;
import com.example.fmanager.models.Category;
import com.example.fmanager.models.Transaction;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryGetDto {
    private Integer id;
    private String name;
    private Set<Integer> budgetIds = new HashSet<>();
    private Set<Integer> transactionIds;
    private Set<String> transactionDescriptions;

    public static CategoryGetDto convertToDto(Category category) {
        CategoryGetDto dto = new CategoryGetDto();
        dto.setId(category.getId());
        Set<Budget> budgets = category.getBudgets();
        if (budgets != null) {
            dto.setBudgetIds(budgets.stream().map(Budget::getId).collect(Collectors.toSet()));
        } else {
            dto.setBudgetIds(new HashSet<>());
        }
        if (category.getTransactions() != null) {
            dto.setTransactionIds(category.getTransactions().stream()
                    .map(Transaction::getId)
                    .collect(Collectors.toSet()));

            dto.setTransactionDescriptions(category.getTransactions().stream()
                    .map(Transaction::getDescription)
                    .collect(Collectors.toSet()));
        } else {
            dto.setTransactionIds(new HashSet<>());
            dto.setTransactionDescriptions(new HashSet<>());
        }
        dto.setName(category.getName());
        return dto;
    }
}