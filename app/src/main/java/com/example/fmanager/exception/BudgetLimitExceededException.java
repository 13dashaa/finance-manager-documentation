package com.example.fmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BudgetLimitExceededException extends RuntimeException {
    public BudgetLimitExceededException(String message) {
        super(message);
    }
}