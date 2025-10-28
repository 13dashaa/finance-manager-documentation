package com.example.fmanager.dto;

import com.example.fmanager.models.Goal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoalGetDto {
    private Integer id;
    private String name;
    private double targetAmount;
    private double currentAmount = 0;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer clientId;
    private String clientUsername;

    public static GoalGetDto convertToDto(Goal goal) {
        GoalGetDto dto = new GoalGetDto();
        dto.setId(goal.getId());
        dto.setClientId(goal.getClient().getId());
        dto.setClientUsername(goal.getClient().getUsername());
        dto.setName(goal.getName());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setStartDate(goal.getStartDate());
        dto.setEndDate(goal.getEndDate());
        return dto;
    }
}
