package com.example.fmanager.dto;

import com.example.fmanager.models.Budget;
import com.example.fmanager.models.Client;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BudgetGetDto {

    private Integer id;
    private Set<Integer> clientIds;
    private Set<String> clientUsernames;
    private Integer categoryId;
    private String categoryName;
    private double limitation;
    private double availableSum;
    private int period;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BudgetGetDto convertToDto(Budget budget) {
        BudgetGetDto dto = new BudgetGetDto();
        dto.setId(budget.getId());
        Set<Client> clients = budget.getClients();
        if (clients != null) {
            dto.setClientIds(budget.getClients().stream()
                    .map(Client::getId)
                    .collect(Collectors.toSet()));
            dto.setClientUsernames(budget.getClients().stream()
                    .map(Client::getUsername)
                    .collect(Collectors.toSet()));
        } else {
            dto.setClientIds(new HashSet<>());
            dto.setClientUsernames(new HashSet<>());
        }
        dto.setCategoryName(budget.getCategory().getName());
        dto.setCategoryId(budget.getCategory().getId());
        dto.setLimitation(budget.getLimitation());
        dto.setAvailableSum(budget.getAvailableSum());
        dto.setPeriod(budget.getPeriod());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());
        return dto;
    }
}

