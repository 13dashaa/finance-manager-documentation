package com.example.fmanager.exception;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(final String message) {
        super(message);
    }
}