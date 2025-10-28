package com.example.fmanager.controller;

import com.example.fmanager.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@Tag(name = "Log Controller", description = "API for logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(summary = "Generate log file for the specified date",
            description = "Starts asynchronous generation of a log file for the specified date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task started successfully"),
        @ApiResponse(responseCode = "500", description = "Server error processing request")
    })
    @PostMapping("/{date}")
    public ResponseEntity<String> generateLogsByDate(@PathVariable LocalDate date) {
        try {
            String dateString = date.toString();
            CompletableFuture<String> future = logService.generateLogFileForDateAsync(dateString);
            return ResponseEntity.accepted().body("Task started. ID: " + future.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Task had been interrupted: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Get task status by ID",
            description = "Returns the status of the task by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task ID not found")
    })
    @GetMapping("/{logId}/status")
    public ResponseEntity<String> getTaskStatus(@PathVariable String logId) {
        if (logService.isTaskCompleted(logId)) {
            return ResponseEntity.ok("Task completed");
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Task not found or not completed");
        }
    }

    @Operation(summary = "Get log file by ID",
            description = "Returns the log file by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File successfully returned"),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "500", description = "Server error processing request")
    })
    @GetMapping("/{logId}/file")
    public ResponseEntity<Resource> getLogFileById(@PathVariable String logId) {
        try {
            String logFilePath = logService.getLogFilePath(logId);
            if (logFilePath == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Path filePath = Paths.get(logFilePath);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + filePath.getFileName().toString())
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}