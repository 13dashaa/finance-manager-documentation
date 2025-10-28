package com.example.fmanager.controller;

import com.example.fmanager.dto.BudgetCreateDto;
import com.example.fmanager.dto.BudgetGetDto;
import com.example.fmanager.dto.BudgetUpdateDto;
import com.example.fmanager.models.Budget;
import com.example.fmanager.service.BudgetService;
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
@RequestMapping("/budgets")
@Tag(name = "Budget Manager", description = "APIs for managing budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @Operation(summary = "Create a new budget")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Budget created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input provided"),
        @ApiResponse(responseCode = "404", description = "Budget not found after creation")
    })
    public ResponseEntity<BudgetGetDto> createBudget(
            @Valid @RequestBody BudgetCreateDto budgetCreateDto
    ) {
        Budget budget = budgetService.createBudget(budgetCreateDto);
        return budgetService
                .getBudgetById(budget.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all budgets")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of budgets retrieved successfully")
    })
    public List<BudgetGetDto> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Budget found"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<BudgetGetDto> getBudget(
            @Parameter(description = "ID of the budget to retrieve", example = "1")
            @PathVariable int id) {
        return budgetService
                .getBudgetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter budgets by client and category",
            description = "Returns budgets matching specified client ID and category ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Budgets found"),
        @ApiResponse(responseCode = "404", description = "Client or category not found"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<BudgetGetDto>> filterBudgets(
            @Parameter(description = "Client ID to filter by", example = "1", required = true)
            @RequestParam int clientId,

            @Parameter(description = "Category ID to filter by", example = "2", required = true)
            @RequestParam int categoryId) {

        List<BudgetGetDto> budgets = budgetService
                .getBudgetsByClientIdAndCategoryId(clientId, categoryId);
        return ResponseEntity.ok(budgets);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Budget by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public void deleteBudget(
            @Parameter(description = "ID of the budget to update", example = "1")
            @PathVariable int id) {
        budgetService.deleteBudget(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget by ID", description = "Updates an existing budget")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
        @ApiResponse(responseCode = "404", description = "Budget not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<BudgetGetDto> updateBudget(
            @Parameter(description = "ID of the budget to update", example = "1")
            @PathVariable int id,
            @Parameter(description = "Updated budget details")
            @RequestBody BudgetUpdateDto budgetDetails) {
        BudgetGetDto updatedBudget = budgetService.updateBudget(id, budgetDetails);
        return ResponseEntity.ok(updatedBudget);
    }
}