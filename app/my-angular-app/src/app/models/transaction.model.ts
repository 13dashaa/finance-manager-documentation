// src/app/models/transaction.model.ts

/**
 * Represents the data structure for displaying a transaction (based on TransactionGetDto).
 */
export interface TransactionGetDto {
  id: number;
  description: string | null;
  amount: number;
  accountId: number;
  accountName: string;
  categoryId: number;
  categoryName: string;
  date: string; // ISO date string
  createdAt: string; // ISO date string
}

/**
 * Represents the payload for creating or updating a transaction (based on TransactionCreateDto).
 */
export interface TransactionCreateDto {
  description?: string;
  amount: number;
  date: string; // Expecting ISO date string format YYYY-MM-DDTHH:mm:ss
  categoryId: number;
  accountId: number;
}

/**
 * Represents the structure for filtering transactions.
 */
export interface TransactionFilterParams {
  clientId: number;
  categoryId: number;
}
