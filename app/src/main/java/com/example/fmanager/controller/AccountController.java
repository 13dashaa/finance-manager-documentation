package com.example.fmanager.controller;

import com.example.fmanager.dto.AccountCreateDto;
import com.example.fmanager.dto.AccountGetDto;
import com.example.fmanager.dto.AccountUpdateDto;
import com.example.fmanager.dto.BulkCreateDto;
import com.example.fmanager.models.Account;
import com.example.fmanager.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
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
@RequestMapping("/accounts")
@Tag(name = "Account Management", description = "APIs for managing accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new account",
            description = "Creates a new account with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account created successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<AccountGetDto> createAccount(
            @Valid @RequestBody AccountCreateDto account
    ) {
        Account newAccount = accountService.createAccount(account);
        return accountService
                .getAccountById(newAccount.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all accounts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public List<AccountGetDto> getAccounts() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves an account by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountGetDto> getAccount(
            @Parameter(description = "ID of the account to retrieve", example = "1")
            @PathVariable int id) {
        return accountService
                .getAccountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    @Operation(summary = "Get accounts by client ID",
            description = "Retrieves accounts associated with a specific client ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid client ID")
    })
    public List<AccountGetDto> getAccountsByClient(
            @Parameter(description = "Client ID to filter accounts", example = "1")
            @RequestParam int clientId) {
        return accountService.findByClientId(clientId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account by ID", description = "Updates an existing account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<AccountGetDto> updateAccount(
            @Parameter(description = "ID of the account to update", example = "1")
            @PathVariable int id,
            @Parameter(description = "Updated account details")
            @RequestBody AccountUpdateDto accountDetails) {
        AccountGetDto updatedAccount = accountService.updateAccount(id, accountDetails);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple accounts",
            description = "Creates multiple accounts with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<List<AccountGetDto>> createAccountsBulk(
            @Valid @RequestBody BulkCreateDto<AccountCreateDto> bulkCreateDto
    ) {
        List<AccountGetDto> createdAccounts = bulkCreateDto.getItems().stream()
                .map(accountCreateDto -> {
                    Account newAccount = accountService.createAccount(accountCreateDto);
                    return accountService.getAccountById(newAccount.getId())
                            .orElseThrow(() ->
                                    new RuntimeException("Account not found after creation"));
                })
                .toList();

        return ResponseEntity.ok(createdAccounts);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public void deleteAccount(
            @Parameter(description = "ID of the account to delete", example = "1")
            @PathVariable int id) {
        accountService.deleteAccount(id);
    }
}