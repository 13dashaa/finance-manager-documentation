package com.example.fmanager.exception;

public class NotFoundMessages {
    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";
    public static final String BUDGET_NOT_FOUND_MESSAGE = "Budget not found";
    public static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found";
    public static final String CLIENT_NOT_FOUND_MESSAGE = "Client not found";
    public static final String GOAL_NOT_FOUND_MESSAGE = "Goal not found";
    public static final String TRANSACTION_NOT_FOUND_MESSAGE = "Transaction not found";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String INVALID_DATA = "Invalid Data";

    private NotFoundMessages() {
        throw new UnsupportedOperationException("Class cannot be instantiated");
    }
}
