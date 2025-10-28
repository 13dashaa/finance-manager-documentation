// src/app/models/category.model.ts

/**
 * Based on CategoryGetDto
 */
export interface CategoryGetDto {
  id: number;
  name: string;
  budgetIds: number[]; // Changed from Set
  transactionIds: number[]; // Changed from Set
  transactionDescriptions: string[]; // Changed from Set
}

/**
 * Based on CategoryCreateDto
 */
export interface CategoryCreateDto {
  name: string;
}
