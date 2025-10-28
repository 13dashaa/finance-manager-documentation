package com.example.fmanager.controller;

import com.example.fmanager.dto.TransactionCreateDto;
import com.example.fmanager.dto.TransactionGetDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Transaction;
import com.example.fmanager.service.TransactionService;
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
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Create a new transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input/Budget limit exceeded"),
        @ApiResponse(responseCode = "404", description = "Account/Category not found"),
        @ApiResponse(responseCode = "422", description = "Business logic error")
    })
    public ResponseEntity<TransactionGetDto> createTransaction(
            @Valid @RequestBody TransactionCreateDto transactionCreateDto) {
        Transaction transaction = transactionService.createTransaction(transactionCreateDto);
        return transactionService.getTransactionById(transaction.getId())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Transaction not found after creation"));
    }

    @GetMapping
    @Operation(summary = "Get all transactions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public List<TransactionGetDto> getTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionGetDto> getTransaction(
            @Parameter(description = "ID of the transaction to retrieve", example = "1")
            @PathVariable int id) {
        return transactionService
                .getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    @Operation(summary = "Get transactions by client and category",
            description = "Retrieves transactions associated with a specific client and category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid client ID or category ID")
    })
    public List<TransactionGetDto> getTransactionsByClientAndCategory(
            @Parameter(description = "ID of the client to filter transactions", example = "1")
            @RequestParam int clientId,
            @Parameter(description = "ID of the category to filter transactions", example = "1")
            @RequestParam int categoryId) {
        return transactionService.findByClientIdAndCategoryId(clientId, categoryId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction by ID",
            description = "Updates an existing transaction with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionGetDto> updateTransaction(
            @Parameter(description = "ID of the transaction to update", example = "1")
            @PathVariable int id,
            @Parameter(description = "Updated transaction details")

            @RequestBody TransactionCreateDto transactionDetails) {
        TransactionGetDto updatedTransaction =
                transactionService.updateTransaction(id, transactionDetails);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public void deleteTransaction(
            @Parameter(description = "ID of the transaction to delete", example = "1")
            @PathVariable int id) {
        transactionService.deleteTransaction(id);
    }
}
