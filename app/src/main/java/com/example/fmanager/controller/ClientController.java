package com.example.fmanager.controller;

import com.example.fmanager.dto.ClientCreateDto;
import com.example.fmanager.dto.ClientGetDto;
import com.example.fmanager.dto.ClientUpdateDto;
import com.example.fmanager.models.Client;
import com.example.fmanager.service.ClientService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
@Tag(name = "Client Management", description = "APIs for managing clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @Operation(summary = "Create a new client",
            description = "Creates a new client with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Client created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Client not found after creation")
    })
    public ResponseEntity<ClientGetDto> createUser(
            @Valid @RequestBody ClientCreateDto clientCreateDto
    ) {
        Client client = clientService.createUser(clientCreateDto);
        return clientService
                .findById(client.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all clients", description = "Retrieves a list of all clients")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Clients retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public List<ClientGetDto> getClients() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID", description = "Retrieves a client by its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Client found"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientGetDto> getClientById(
            @Parameter(description = "ID of the client to retrieve", example = "1")
            @PathVariable int id) {
        return clientService
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete client by ID", description = "Deletes a client by its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Client deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public void deleteClient(
            @Parameter(description = "ID of the client to delete", example = "1")
            @PathVariable int id) {
        clientService.deleteUser(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client by ID",
            description = "Updates an existing client with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Client updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientGetDto> updateClient(
            @Parameter(description = "ID of the client to update", example = "1")
            @PathVariable int id,
            @Parameter(description = "Updated client details")
            @RequestBody ClientUpdateDto clientDetails) {
        ClientGetDto updatedClient = clientService.updateUser(id, clientDetails);
        return ResponseEntity.ok(updatedClient);
    }
}