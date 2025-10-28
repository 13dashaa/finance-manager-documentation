package com.example.fmanager.controller;

import com.example.fmanager.dto.GoalCreateDto;
import com.example.fmanager.dto.GoalGetDto;
import com.example.fmanager.dto.TransactionCreateDto;
import com.example.fmanager.exception.InvalidDataException;
import com.example.fmanager.models.Goal;
import com.example.fmanager.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goals")
@Tag(name = "Goal Management", description = "APIs for managing goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    @Operation(summary = "Create a new goal",
            description = "Creates a new goal with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Goal created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Goal not found after creation")
    })
    public ResponseEntity<GoalGetDto> createGoal(@Valid @RequestBody GoalCreateDto goalCreateDto) {
        Goal goal = goalService.createGoal(goalCreateDto);
        return goalService
                .getGoalById(goal.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all goals", description = "Retrieves a list of all goals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Goals retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public List<GoalGetDto> getGoals() {
        return goalService.getAllGoals();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by ID", description = "Retrieves a goal by its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Goal found"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<GoalGetDto> getGoalById(
            @Parameter(description = "ID of the goal to retrieve", example = "1")
            @PathVariable int id) {
        return goalService
                .getGoalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/add-funds") // POST на ресурс цели с указанием действия
    @Operation(summary = "Add funds towards a specific goal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                    description = "Funds added successfully, returns updated goal"),
        @ApiResponse(responseCode = "400",
                    description = "Invalid input (e.g., negative amount)"),
        @ApiResponse(responseCode = "404",
                    description = "Goal or Account not found")
    })
    public ResponseEntity<GoalGetDto> addFundsToGoal(
            @Parameter(description = "ID of the goal to add funds to", required = true)
            @PathVariable int id,
            @Parameter(description = "Transaction details", required = true)
            @Valid @RequestBody TransactionCreateDto transactionDto
    ) {
        if (transactionDto.getAmount() <= 0) {
            throw new InvalidDataException("Amount to save must be positive.");
        }
        GoalGetDto updatedGoal = goalService.addFundsToGoal(id, transactionDto);
        return ResponseEntity.ok(updatedGoal);
    }

    @GetMapping("/filter")
    @Operation(summary = "Get goals by client ID",
            description = "Retrieves goals associated with a specific client ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Goals retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid client ID")
    })
    public List<GoalGetDto> getGoalsByClient(
            @Parameter(description = "Client ID to filter goals", example = "1")
            @RequestParam int clientId) {
        return goalService.findByClientId(clientId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal by ID", description = "Deletes a goal by its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Goal deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public void deleteGoal(
            @Parameter(description = "ID of the goal to delete", example = "1")
            @PathVariable int id) {
        goalService.deleteGoal(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update goal by ID",
            description = "Updates an existing goal with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Goal updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    public ResponseEntity<GoalGetDto> updateGoal(
            @Parameter(description = "ID of the goal to update", example = "1")
            @PathVariable int id,
            @Parameter(description = "Updated goal details")
            @RequestBody GoalCreateDto goalDetails) {
        GoalGetDto updatedGoal = goalService.updateGoal(id, goalDetails);
        return ResponseEntity.ok(updatedGoal);
    }
}